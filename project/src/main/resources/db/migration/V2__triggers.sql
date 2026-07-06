-- Triger - Zadatak 1 - Nenad
-- Kratak opis: Implementirati automatsko ograničenje dubine ugnježdavanja komentara.
-- Plan implementacije: Kreiranje trigera koji će se aktivirati pre unosa komentara i nakon toga
-- će rekurzivno proveriti da li bi komentar bio odgovor na neki drugi komentar i u slučaju da
-- nije, onda će se komentar normalno upisati, dok u slučaju da jeste odgovor, odnosno ako je
-- na dubini većoj od 5 u samom stablu rekurzije onda treba blokirati sam unos komentara uz
-- ispis poruke korisniku. Na nivou 1 se nalazi sam korenski komentar, dok se komentari na
-- nivou 5 još uvek prihvataju, a sve posle toga, odnosno komentari na nivou 6 će biti odbijeni.

CREATE OR REPLACE FUNCTION proveri_dubinu_komentara()
RETURNS TRIGGER AS $$
DECLARE
    dubina_roditeljskog_komentara INTEGER;
    dubina_novog_komentara INTEGER;
BEGIN
    -- Slucaj ako je u pitanju korenski komentar
    IF NEW.id_ok IS NULL THEN
       RETURN NEW;
    END IF;

    -- Rekurzivno se krecemo ka korenskom komentaru
WITH RECURSIVE lanac_komentara AS (
    SELECT id_k, id_ok, 1 AS nivo FROM komentar WHERE id_k = NEW.id_ok
    UNION ALL
    SELECT k.id_k, k.id_ok, lk.nivo + 1 FROM komentar k INNER JOIN lanac_komentara lk ON k.id_k = lk.id_ok
)
SELECT COALESCE(MAX(nivo), 0) INTO dubina_roditeljskog_komentara FROM lanac_komentara WHERE id_ok IS NULL;

dubina_novog_komentara := dubina_roditeljskog_komentara + 1;
    IF dubina_novog_komentara > 5 THEN
        RAISE EXCEPTION 'MAX_DUBINA_PREMASENA: Dostignut je maksimalan broj nivoa ugnjezdavanja komentara. Dodavanje odgovora na komentar nije moguce!';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_provera_dubine_komentara ON komentar;

CREATE TRIGGER trigger_provera_dubine_komentara
    BEFORE INSERT ON komentar
    FOR EACH ROW
    EXECUTE FUNCTION proveri_dubinu_komentara();

-- Kraj zadatka 1

-- Izvestaj - Zadatak 2 - Nenad
-- Kratak opis: Implementirati izveštajnu funkcionalnost za praćenje aktivnosti korisnika nad
-- digitalnim sadržajem po mesecima.
-- Plan implementacije: Potrebno je kreirati funkciju koja će, upotrebom kurstora, prolaziti kroz
-- sve mesece zadate godine i za svaki mesec će izračunati:
-- - Ukupan broj čitanja e-knjiga kojima je datum poslednjeg pristupa u tom mesecu.
-- - Ukupan broj slušanja audio knjiga kojima je datum poslednjeg pristupa u tom
-- mesecu.
-- - Ukupan broj preuzimanja elektronskih baza podataka u tom mesecu.
-- - Ukupan broj kreiranih čet sesija u tom mesecu, kao i statistika o prosečnom i
-- ukupnom broju poslatih poruka ka AI agentu.
-- Rezultat je kolekcija redova ispisana I sortirana po mesecima u rastućem redosledu i sa
-- ukupnim zbirom, odnosno prosekom, na kraju za celokupnu godinu.

CREATE OR REPLACE FUNCTION izvestaj_aktivnosti_po_mesecima(p_godina INTEGER)
RETURNS TABLE (
    mesec                        INTEGER,
    broj_citanja_eknjiga         BIGINT,
    broj_slusanja_audio          BIGINT,
    broj_preuzimanja_baza        BIGINT,
    broj_cet_sesija              BIGINT,
    ukupno_poruka_ka_ai          BIGINT,
    prosek_poruka_po_sesiji      NUMERIC
) AS $$
DECLARE
    mesec_cursor CURSOR FOR SELECT gs AS mesec_broj FROM generate_series(1, 12) AS gs ORDER BY gs;

    v_mesec         INTEGER;
    v_citanja       BIGINT;
    v_slusanja      BIGINT;
    v_preuzimanja   BIGINT;
    v_sesije        BIGINT;
    v_poruke_ai     BIGINT;
    v_prosek        NUMERIC;
BEGIN
    OPEN mesec_cursor;

    LOOP
        FETCH mesec_cursor INTO v_mesec;
        EXIT WHEN NOT FOUND;

        -- 1. Broj citanja e-knjiga na osnovu datuma poslednjeg pristupa u datom mesecu
        SELECT COUNT(*) INTO v_citanja FROM citanje_eknjige ck
        WHERE EXTRACT(YEAR FROM ck.datum_poslednjeg_pristupa_ck) = p_godina
        AND EXTRACT(MONTH FROM ck.datum_poslednjeg_pristupa_ck) = v_mesec;

        -- 2. Broj slusanja audio knjiga na osnovu datuma poslednjeg pristupa u datom mesecu
        SELECT COUNT(*) INTO v_slusanja FROM slusanje_audio_knjige sak
        WHERE EXTRACT(YEAR  FROM sak.datum_poslednjeg_pristupa_sak) = p_godina
        AND EXTRACT(MONTH FROM sak.datum_poslednjeg_pristupa_sak) = v_mesec;

        -- 3. Broj preuzimanja ebp u datom mesecu
        SELECT COUNT(*) INTO v_preuzimanja FROM preuzimanje_baze_podataka pbp
        WHERE EXTRACT(YEAR  FROM pbp.datum_preuzimanja_pbp) = p_godina
        AND EXTRACT(MONTH FROM pbp.datum_preuzimanja_pbp) = v_mesec;

        -- 4. Broj kreiranih cet sesija u datom mesecu
        SELECT COUNT(*) INTO v_sesije FROM cet_sesija cs
        WHERE EXTRACT(YEAR  FROM cs.datum_kreiranja_cs) = p_godina
        AND EXTRACT(MONTH FROM cs.datum_kreiranja_cs) = v_mesec;

        -- Ukupan i prosecan broj poruka ka AI agentu u sesijama kreiranim u datom mesecu
        SELECT COUNT(cp.id_cp) INTO v_poruke_ai FROM cet_poruka cp JOIN cet_sesija cs2 ON cs2.id_cs = cp.id_cs
        WHERE EXTRACT(YEAR  FROM cs2.datum_kreiranja_cs) = p_godina
        AND EXTRACT(MONTH FROM cs2.datum_kreiranja_cs) = v_mesec
        AND cp.tip_cp = 'CLAN';

        -- Racunanje proseka broja poruka u sesiji za taj mesec
        v_prosek := CASE WHEN v_sesije = 0 THEN 0
                    ELSE ROUND(v_poruke_ai::NUMERIC / v_sesije, 2)
                    END;

        -- Unos i konacnu tabelu
        mesec                   := v_mesec;
        broj_citanja_eknjiga    := v_citanja;
        broj_slusanja_audio     := v_slusanja;
        broj_preuzimanja_baza   := v_preuzimanja;
        broj_cet_sesija         := v_sesije;
        ukupno_poruka_ka_ai     := v_poruke_ai;
        prosek_poruka_po_sesiji := v_prosek;

        RETURN NEXT;
    END LOOP;

    CLOSE mesec_cursor;
END;
$$ LANGUAGE plpgsql;

-- Kraj zadatka 2

-- nesto - Zadatak 3 - Nenad

-- =====================================================================
-- V1: Indeksi za optimizaciju upita "najcitanije knjige po zanru
-- u poslednjih 30 dana"
--
-- Upit koji se optimizuje (KnjigaRepository.findNajcitanijeKnjigePoZanru):
--
--   SELECT k.isbn, k.naslov, COUNT(c.jmbg_clana) AS broj_citanja
--   FROM knjiga k
--   JOIN citanje_eknjige c ON c.isbn_eknjige = k.isbn
--   WHERE k.zanr_id = :zanrId
--     AND c.datum_poslednjeg_pristupa_ck >= CURRENT_DATE - INTERVAL '30 days'
--   GROUP BY k.isbn, k.naslov
--   ORDER BY broj_citanja DESC;
--
-- Plan izvrsavanja PRE ovih indeksa (nad opterecenom tabelom generisanom
-- skriptom generate_test_data.sql, ~85.000 redova u citanje_eknjige,
-- ~5.000 redova u knjiga) je koristio Seq Scan nad obe tabele. Sa
-- indeksima ispod planer prelazi na Index/Bitmap Index Scan.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Indeks nad knjiga.zanr_id
--
--    Koristi se u WHERE k.zanr_id = :zanrId. Bez ovog indeksa planer
--    mora da procita celu tabelu knjiga da bi nasao knjige zadatog zanra.
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_knjiga_zanr_id
    ON knjiga (zanr_id);

-- ---------------------------------------------------------------------
-- 2. Kompozitni indeks nad citanje_eknjige (isbn_eknjige, datum_poslednjeg_pristupa_ck)
--
--    Ovo je najvazniji indeks za ovaj upit. Redosled kolona nije slucajan:
--    - isbn_eknjige je prva kolona jer se koristi u JOIN uslovu
--      (c.isbn_eknjige = k.isbn) i ima visoku selektivnost (mnogo
--      razlicitih ISBN vrednosti).
--    - datum_poslednjeg_pristupa_ck je druga kolona jer se filtrira
--      sa >= (range predikat), sto Postgres moze efikasno da iskoristi
--      kao "nastavak" indeksa nakon sto pozicionira isbn_eknjige.
--
--    Ovaj indeks pokriva i JOIN i WHERE deo upita u jednom prolazu kroz
--    B-tree, sto je bolje nego dva odvojena indeksa nad svakom kolonom.
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_citanje_isbn_datum_pristupa
    ON citanje_eknjige (isbn_eknjige, datum_poslednjeg_pristupa_ck);

-- ---------------------------------------------------------------------
-- 3. Samostalni indeks nad datum_poslednjeg_pristupa_ck
--
--    Koristan za upite koji filtriraju SAMO po datumu pristupa bez
--    JOIN-a na isbn (npr. izvestaji o aktivnosti citanja u odredjenom
--    periodu, bez obzira na knjigu). Nije strogo neophodan za nas
--    konkretan upit (kompozitni indeks iz koraka 2 ga vec pokriva u
--    kombinaciji sa JOIN-om), ali se dodaje jer se ista kolona koristi
--    i u drugim upitima repozitorijuma (findActiveByJmbg, findExpiredActive).
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_citanje_datum_pristupa
    ON citanje_eknjige (datum_poslednjeg_pristupa_ck);

-- Kraj zadatka 3