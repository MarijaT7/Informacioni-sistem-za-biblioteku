-- =============================================================================================================================================================
-- =============================================================================================================================================================
-- =============================================================================================================================================================
-- =============================================================================================================================================================
-- =============================================================================================================================================================

--                                              Marijini dodatni zadaci iz sbp-a


-- =============================================================================================================================================================
--                                                      Zadatak 1

-- Pomocna funkcija koja nalazi zanr za svaku stavku narudzbine
-- Ako je:
--  1. postojeca knjiga    -> knjiga.zanrId
--  2. sistemska preporuka -> fizickaKnjiga.knjiga.zanrId
--  3. predlog korisnika   -> predlog_za_nabavku.zanrId

CREATE OR REPLACE FUNCTION fn_odredi_zanr_stavke(
    p_isbn        VARCHAR,
    p_predlog_id  BIGINT,
    p_preporuka_id BIGINT
) RETURNS BIGINT AS $$

DECLARE
v_zanr_id BIGINT;

BEGIN
    IF p_isbn IS NOT NULL THEN
        SELECT k.zanr_id
        INTO v_zanr_id
        FROM knjiga k
        WHERE k.isbn = p_isbn;

    ELSIF p_predlog_id IS NOT NULL THEN
        SELECT p.zanr_id
        INTO v_zanr_id
        FROM predlog_za_nabavku p
        WHERE p.id = p_predlog_id;

    ELSIF p_preporuka_id IS NOT NULL THEN
        SELECT k.zanr_id INTO v_zanr_id
        FROM sistemska_preporuka sp
        JOIN knjiga k ON k.isbn = sp.isbn
        WHERE sp.id = p_preporuka_id;
    END IF;
    RETURN v_zanr_id; -- moze ostati NULL - stavka se tada ne knjizi na budzet
END;
$$ LANGUAGE plpgsql;


-- Triger 1 -> njegov posao je da rezervise sredsva i odbije dodavanje stavke ako nema dovoljno novca za taj neki zanr !


CREATE OR REPLACE FUNCTION trg_fn_stavka_pre_insert()
       RETURNS TRIGGER AS $$

DECLARE
    v_zanr_id   BIGINT;
    v_dostupno  DOUBLE PRECISION;
    v_naziv_zanra VARCHAR;

BEGIN
    v_zanr_id := fn_odredi_zanr_stavke(NEW.isbn, NEW.predlog_id, NEW.preporuka_id);

    -- ako ne mogu da odredimo zanr, ne blokiram unos
    IF v_zanr_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT (bpz.ukupan_budzet - bpz.potroseno - COALESCE(bpz.rezervisano,0)), z.zanr_name
    INTO v_dostupno, v_naziv_zanra
    FROM budzet_po_zanru bpz JOIN zanr z ON z.zanr_id = bpz.zanr_id
    WHERE bpz.zanr_id = v_zanr_id
    FOR UPDATE;                                                                 -- koristim kako bih zakljucala ovaj red dok se moja transakcija ne izvrsi do kraja
                                                                                --izbegava se anomalija azuriranja "lost update"

    IF NOT FOUND THEN
            RAISE EXCEPTION 'Za žanr id=% nije definisan budžet - narudžbina se ne može evidentirati.', v_zanr_id;
    END IF;

    IF v_dostupno < NEW.ukupna_cena_stavke THEN
        RAISE EXCEPTION 'Nedovoljno raspoloživih sredstava u budžetu za žanr "%": dostupno % RSD, potrebno % RSD.', v_naziv_zanra, ROUND(v_dostupno::numeric,2), ROUND(NEW.ukupna_cena_stavke::numeric,2);
    END IF;

    UPDATE budzet_po_zanru
    SET rezervisano = COALESCE(rezervisano,0) + NEW.ukupna_cena_stavke
    WHERE zanr_id = v_zanr_id;

    RETURN NEW;

END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_stavka_pre_insert ON stavka_narudzbine;
CREATE TRIGGER trg_stavka_pre_insert
    BEFORE INSERT ON stavka_narudzbine
    FOR EACH ROW
    EXECUTE FUNCTION trg_fn_stavka_pre_insert();


-- Triger 2 -> ako je narudzbina i dalje u statusu kreirana moguce je brisati stavke iz kao korpe, pri tom se oslobadjaju sredstva iz budzeta za taj zanr kome pripada obrisana stavka

CREATE OR REPLACE FUNCTION trg_fn_stavka_posle_delete()
       RETURNS TRIGGER AS $$

DECLARE
    v_zanr_id BIGINT;
    v_status  VARCHAR;

BEGIN
    SELECT status
    INTO v_status
    FROM narudzbina
    WHERE id = OLD.narudzbina_id;

    IF v_status = 'KREIRANA' THEN
        v_zanr_id := fn_odredi_zanr_stavke(OLD.isbn, OLD.predlog_id, OLD.preporuka_id);
        IF v_zanr_id IS NOT NULL THEN
            UPDATE budzet_po_zanru
            SET rezervisano = GREATEST(0, COALESCE(rezervisano,0) - OLD.ukupna_cena_stavke)
            WHERE zanr_id = v_zanr_id;
        END IF;
    END IF;
RETURN OLD;                         --old -> red kakav je bio pre promene

END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_stavka_posle_delete ON stavka_narudzbine;
CREATE TRIGGER trg_stavka_posle_delete
    AFTER DELETE ON stavka_narudzbine
    FOR EACH ROW
    EXECUTE FUNCTION trg_fn_stavka_posle_delete();


-- Triger 3 -> Kada narudzbina predje iz stanja KREIRANA u POTVRDJENAA/ISPORUCENA
CREATE OR REPLACE FUNCTION trg_fn_narudzbina_status_change()
       RETURNS TRIGGER AS $$

DECLARE
r RECORD;
    v_zanr_id BIGINT;

BEGIN
    IF NEW.status IS DISTINCT FROM OLD.status
       AND OLD.status = 'KREIRANA'
       AND NEW.status IN ('ISPORUCENA', 'POTVRDJENA') THEN
        FOR r IN SELECT * FROM stavka_narudzbine WHERE narudzbina_id = NEW.id LOOP
            v_zanr_id := fn_odredi_zanr_stavke(r.isbn, r.predlog_id, r.preporuka_id);
            IF v_zanr_id IS NOT NULL THEN
                UPDATE budzet_po_zanru
                SET rezervisano = GREATEST(0, COALESCE(rezervisano,0) - r.ukupna_cena_stavke),
                    potroseno   = potroseno + r.ukupna_cena_stavke
                WHERE zanr_id = v_zanr_id;
            END IF;
        END LOOP;
    END IF;
RETURN NEW;                     --new je red kakav treba da bude upisan
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_narudzbina_status_change ON narudzbina;
CREATE TRIGGER trg_narudzbina_status_change
    BEFORE UPDATE ON narudzbina
    FOR EACH ROW
    EXECUTE FUNCTION trg_fn_narudzbina_status_change();



-- =============================================================================================================================================================
--                                                      Zadatak 2
--                                        Izvestajna funkcija za nabavke po mesecima

-- Ideja je da upotrebom kursora prolayim kroz svaki od 12 meseci izabrane godine i za svaki mesec prikupim podatke o broju narudzbina,
-- ukupnoj vrednosti narudzbi, broju isporuka, broju isporuka na vreme i isporuka koje kasne, prosecno kasnjenje u danima, broju reklamacija i broju
-- resenih reklamacija. Na kraju se akumuliraju podaci za godisnji sumarni red.

CREATE OR REPLACE FUNCTION fn_izvestaj_nabavke_po_mesecima(
    p_godina        INTEGER,
    p_dobavljac_id  BIGINT DEFAULT NULL             -- ako je NULL, izvestaj je za sve dobavljace
)
    RETURNS TABLE (
        mesec                     INTEGER,          -- 1-12, a 0 = "UKUPNO/PROSEK ZA GODINU"
        naziv_meseca              VARCHAR,
        broj_narudzbina           INTEGER,
        ukupna_vrednost_narudzbi  NUMERIC,
        broj_isporuka             INTEGER,
        broj_isporuka_na_vreme    INTEGER,
        broj_isporuka_kasni       INTEGER,
        prosecno_kasnjenje_dana   NUMERIC,
        broj_reklamacija          INTEGER,
        broj_resenih_reklamacija  NUMERIC
    ) AS $$

DECLARE
    -- kursor koji vrti sve mesece (1..12) zadate godine
    mesec_cursor CURSOR FOR
        SELECT gm AS br_meseca, TO_CHAR(TO_DATE(gm::text,'MM'), 'TMMonth') AS naziv
        FROM generate_series(1,12) AS gm;

    v_mesec_rec       RECORD;
    v_broj_narudzbina INTEGER;
    v_ukupna_vrednost NUMERIC;
    v_broj_isporuka   INTEGER;
    v_na_vreme        INTEGER;
    v_kasni           INTEGER;
    v_prosek_kasnjenja NUMERIC;
    v_broj_reklamacija INTEGER;
    v_resene_reklamacije INTEGER;

    -- akumulatori za godisnji sumarni red
    sum_narudzbina INTEGER := 0;
    sum_vrednost   NUMERIC := 0;
    sum_isporuka   INTEGER := 0;
    sum_na_vreme   INTEGER := 0;
    sum_kasni      INTEGER := 0;
    sum_kasnjenje_dana NUMERIC := 0;                    -- suma dana kasnjenja (za prosek na kraju)
    sum_reklamacija INTEGER := 0;
    sum_resene      INTEGER := 0;

BEGIN
    OPEN mesec_cursor;
    LOOP
        FETCH mesec_cursor INTO v_mesec_rec;
                EXIT WHEN NOT FOUND;

        -- broj narudzbina kreiranih u datom mesecu i njihova ukupna vrednost
        SELECT COUNT(*), COALESCE(SUM(n.ukupna_cena),0)
        INTO v_broj_narudzbina, v_ukupna_vrednost
        FROM narudzbina n
        WHERE EXTRACT(YEAR FROM n.datum_kreiranja) = p_godina
                AND EXTRACT(MONTH FROM n.datum_kreiranja) = v_mesec_rec.br_meseca
                AND (p_dobavljac_id IS NULL OR n.dobavljac_id = p_dobavljac_id);

        -- isporuke realizovane u datom mesecu: na vreme vs sa yakasnjenjem + prosecno kasnjenje
        SELECT COUNT(*),
               COUNT(*) FILTER (WHERE n.datum_stvarne_isporuke <= n.datum_ocekivane_isporuke),
               COUNT(*) FILTER (WHERE n.datum_stvarne_isporuke  > n.datum_ocekivane_isporuke),
               COALESCE(AVG(GREATEST(0, n.datum_stvarne_isporuke - n.datum_ocekivane_isporuke))
               FILTER (WHERE n.datum_stvarne_isporuke > n.datum_ocekivane_isporuke), 0)
        INTO v_broj_isporuka, v_na_vreme, v_kasni, v_prosek_kasnjenja
        FROM narudzbina n
        WHERE n.datum_stvarne_isporuke IS NOT NULL
          AND EXTRACT(YEAR FROM n.datum_stvarne_isporuke) = p_godina
          AND EXTRACT(MONTH FROM n.datum_stvarne_isporuke) = v_mesec_rec.br_meseca
          AND (p_dobavljac_id IS NULL OR n.dobavljac_id = p_dobavljac_id);

        -- reklamacije podnete u datom mesecu (i koliko je od njih resenih)
        SELECT COUNT(*), COUNT(*) FILTER (WHERE r.status = 'RESENA')
        INTO v_broj_reklamacija, v_resene_reklamacije
        FROM reklamacija r
                 JOIN narudzbina n ON n.id = r.narudzbina_id
        WHERE EXTRACT(YEAR FROM r.datum_podnosenja) = p_godina
          AND EXTRACT(MONTH FROM r.datum_podnosenja) = v_mesec_rec.br_meseca
          AND (p_dobavljac_id IS NULL OR n.dobavljac_id = p_dobavljac_id);

        -- akumulacija za godisnji red
        sum_narudzbina := sum_narudzbina + v_broj_narudzbina;
        sum_vrednost    := sum_vrednost + v_ukupna_vrednost;
        sum_isporuka    := sum_isporuka + v_broj_isporuka;
        sum_na_vreme    := sum_na_vreme + v_na_vreme;
        sum_kasni       := sum_kasni + v_kasni;
        sum_kasnjenje_dana := sum_kasnjenje_dana + (v_prosek_kasnjenja * v_kasni); -- vracamo u "sumu dana" da prosek na kraju bude tacan
        sum_reklamacija := sum_reklamacija + v_broj_reklamacija;
        sum_resene      := sum_resene + v_resene_reklamacije;

        mesec := v_mesec_rec.br_meseca;
        naziv_meseca := INITCAP(v_mesec_rec.naziv);
        broj_narudzbina := v_broj_narudzbina;
        ukupna_vrednost_narudzbi := ROUND(v_ukupna_vrednost, 2);
        broj_isporuka := v_broj_isporuka;
        broj_isporuka_na_vreme := v_na_vreme;
        broj_isporuka_kasni := v_kasni;
        prosecno_kasnjenje_dana := ROUND(v_prosek_kasnjenja, 2);
        broj_reklamacija := v_broj_reklamacija;
        broj_resenih_reklamacija := v_resene_reklamacije;

        RETURN NEXT;
    END LOOP;
    CLOSE mesec_cursor;

    -- sumarni/prosecni red za celu godinu (mesec = 0)
    mesec := 0;
    naziv_meseca := 'UKUPNO/PROSEK ZA GODINU';
    broj_narudzbina := sum_narudzbina;
    ukupna_vrednost_narudzbi := ROUND(sum_vrednost, 2);
    broj_isporuka := sum_isporuka;
    broj_isporuka_na_vreme := sum_na_vreme;
    broj_isporuka_kasni := sum_kasni;
    prosecno_kasnjenje_dana := CASE WHEN sum_kasni > 0
                                     THEN ROUND(sum_kasnjenje_dana / sum_kasni, 2)
                                     ELSE 0 END;
    broj_reklamacija := sum_reklamacija;
    broj_resenih_reklamacija := sum_resene;

    RETURN NEXT;
END;

$$ LANGUAGE plpgsql;



-- =============================================================================================================================================================
--                                                      Zadatak 3
--                                        Analiza i optimizacija upita pomocu indeksa



-- Upit glasi: Pronaći dobavljače i ukupnu vrednost isporučene robe (u poslednjih N dana) za dati žanr, sortirano po vrednosti opadajuće.
-- TAKAV UPIT bez indeksa:

-- SELECT d.id, d.naziv, SUM(sn.ukupna_cena_stavke) AS ukupna_vrednost
-- FROM dobavljac d
-- JOIN narudzbina n ON n.dobavljac_id = d.id
-- JOIN stavka_narudzbine sn ON sn.narudzbina_id = n.id
-- JOIN knjiga k ON k.isbn = sn.isbn
-- WHERE k.zanr_id = ?
--      AND n.status = 'ISPORUCENA'
--      AND n.datum_stvarne_isporuke >= CURRENT_DATE - INTERVAL '90 days'
-- GROUP BY d.id, d.naziv
-- ORDER BY ukupna_vrednost DESC;
--
--
-- Problem je sto ne postoji nijedan indeks koji odgovara WHERE/JOIN uslovima,
-- pa optimizator mora da pročita cele tabele (Seq Scan) i tek onda
-- filtrira/spaja redove.


-- 1) INDEKSI

CREATE INDEX IF NOT EXISTS idx_narudzbina_status_datum_isporuke
    ON narudzbina (status, datum_stvarne_isporuke)
    WHERE status IN ('ISPORUCENA', 'POTVRDJENA');


CREATE INDEX IF NOT EXISTS idx_narudzbina_dobavljac_id
    ON narudzbina (dobavljac_id);
CREATE INDEX IF NOT EXISTS idx_stavka_narudzbina_id
    ON stavka_narudzbine (narudzbina_id);


CREATE INDEX IF NOT EXISTS idx_knjiga_zanr_id
    ON knjiga (zanr_id);


CREATE INDEX IF NOT EXISTS idx_stavka_narudzbina_isbn_covering
    ON stavka_narudzbine (narudzbina_id) INCLUDE (isbn, ukupna_cena_stavke);

ANALYZE narudzbina;
ANALYZE stavka_narudzbine;
ANALYZE knjiga;
ANALYZE dobavljac;


-- 2) RESTRUKTUIRANI UPITTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT

-- Razlike u odnosu na originalni upzt:
--  - filtriranje po žanru se radi ranije, u posebnom CTE-u nad manjom
--    tabelom (knjiga), umesto da se zanr_id proverava tek nakon što se
--    spoje sve stavke i narudžbine
--  - JOIN redosled prati selektivnost filtera (prvo status/datum na
--    narudžbini, zatim žanr na stavkama) što planeru daje bolje granice
--    za korišćenje novih indeksa;
--  - eksplicitno gradimo predikat i za n.status IN ('ISPORUCENA') da
--    odgovara parcijalnom indeksu.

CREATE OR REPLACE FUNCTION fn_top_dobavljaci_po_zanru(
    p_zanr_id   BIGINT,
    p_broj_dana INTEGER DEFAULT 90
)
RETURNS TABLE (dobavljac_id BIGINT, naziv VARCHAR, ukupna_vrednost NUMERIC)
AS $$

BEGIN
    RETURN QUERY
        WITH knjige_zanra AS (
            SELECT k.isbn
            FROM knjiga k
            WHERE k.zanr_id = p_zanr_id
        ),

        isporucene_narudzbine AS (
            SELECT n.id, n.dobavljac_id
            FROM narudzbina n
            WHERE n.status = 'ISPORUCENA'
                AND n.datum_stvarne_isporuke >= CURRENT_DATE - (p_broj_dana || ' days')::interval
        )
    SELECT d.id, d.naziv, SUM(sn.ukupna_cena_stavke) AS ukupna_vrednost
    FROM isporucene_narudzbine n
             JOIN stavka_narudzbine sn ON sn.narudzbina_id = n.id
             JOIN knjige_zanra kz ON kz.isbn = sn.isbn
             JOIN dobavljac d ON d.id = n.dobavljac_id
    GROUP BY d.id, d.naziv
    ORDER BY ukupna_vrednost DESC;

END;
$$ LANGUAGE plpgsql STABLE;

