-- noinspection SqlNoDataSourceInspectionForFile

-- Biblioteke 
INSERT INTO biblioteka (bid, name, ziro_rb) VALUES
('BIB001', 'Gradska biblioteka Novi Sad', '840-123456-78'),
('BIB002', 'Biblioteka Matica srpska','840-234567-89'),
('BIB003', 'Biblioteka Zmaj','840-345678-90');

-- Katalozi
INSERT INTO katalog (kat_ime, standard, bib_id, deleted) VALUES ('Srpska književnost', 'MARC', 'BIB001', FALSE);
INSERT INTO katalog (kat_ime, standard, bib_id, deleted) VALUES ('Jugoslovenska književnost', 'MARC', 'BIB002', FALSE);
INSERT INTO katalog (kat_ime, standard, bib_id, deleted) VALUES ('Savremena proza', 'MARC', 'BIB003', FALSE);


-- KNJIGA 1: Samo Fizička knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150001', 'Na Drini ćuprija', './src/main/resources/knjige/naslovne/cuprija.jpg', 'Roman o istoriji višegradskog mosta.', 1, FALSE, '100', 'Ivo Andrić');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150001');


-- KNJIGA 2: Samo E-Knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150002', 'Prokleta avlija', './src/main/resources/knjige/naslovne/avlija.jpg', 'Priča o zatvorenicima u istanbulskom zatvoru.', 1, FALSE, '010', 'Ivo Andrić');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150002', 'PDF', '2026-01-15', 116, './src/main/resources/knjige/eknjige/avlija.pdf');


-- KNJIGA 3: Samo Audio knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150003', 'Seobe', './src/main/resources/knjige/naslovne/seobe.jpg', 'Istorijski roman o seobama Srba.', 1, FALSE, '001', 'Miloš Crnjanski');

INSERT INTO audio_knjiga (isbn, trajanje_sek_ak, format_ak, datum_dodavanja_ak, putanja_ak)
VALUES ('9788617150003', 10, 'MP3', '2026-02-10', './src/main/resources/knjige/audioknjige/seobe.mp3');


-- KNJIGA 4: Sva tri formata
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150004', 'Gorski vijenac', './src/main/resources/knjige/naslovne/gorski.jpg', 'Poem epske fantastike i filozofije.', 2, FALSE, '111', 'Petar Petrović Njegoš');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150004');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150004', 'PDF', '2026-03-05', 89, './src/main/resources/knjige/eknjige/gorski.pdf');

INSERT INTO audio_knjiga (isbn, trajanje_sek_ak, format_ak, datum_dodavanja_ak, putanja_ak)
VALUES ('9788617150004', 10, 'MP3', '2026-03-01', './src/main/resources/knjige/audioknjige/gorski.mp3');


-- KNJIGA 5: Kombinacija: Fizička i E-Knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150005', 'Hazarski rečnik', './src/main/resources/knjige/naslovne/hazari.jpg', 'Roman leksikon u ženskom i muškom primerku.', 2, FALSE, '110', 'Milorad Pavić');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150005');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150005', 'PDF', '2026-04-12', 13, './src/main/resources/knjige/eknjige/hazari.pdf');


-- KNJIGA 6: Kombinacija: E-Knjiga i Audio knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150006', 'Koreni', './src/main/resources/knjige/naslovne/koreni.jpg', 'Roman o porodici Katić.', 2, FALSE, '011', 'Dobrica Ćosić');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150006', 'PDF', '2026-04-20', 248, './src/main/resources/knjige/eknjige/koreni.pdf');

INSERT INTO audio_knjiga (isbn, trajanje_sek_ak, format_ak, datum_dodavanja_ak, putanja_ak)
VALUES ('9788617150006', 10, 'MP3', '2026-04-22', './src/main/resources/knjige/audioknjige/koreni.mp3');


-- KNJIGA 7: Kombinacija: Fizička i Audio knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150007', 'Tvrđava', './src/main/resources/knjige/naslovne/tvrdjava.jpg', 'Priča o Ahmetu Šabi i njegovom mjestu u svijetu.', 3, FALSE, '101', 'Meša Selimović');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150007');

INSERT INTO audio_knjiga (isbn, trajanje_sek_ak, format_ak, datum_dodavanja_ak, putanja_ak)
VALUES ('9788617150007', 10, 'MP3', '2026-05-01', './src/main/resources/knjige/audioknjige/tvrdjava.mp3');


-- KNJIGA 8: Samo Fizička knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150008', 'Derviš i smrt', './src/main/resources/knjige/naslovne/dervis.jpg', 'Psihološko-filozofski roman Meše Selimovića.', 3, FALSE, '100', 'Meša Selimović');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150008');


-- KNJIGA 9: Samo E-Knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150009', 'Znakovi pored puta', './src/main/resources/knjige/naslovne/znakovi.jpg', 'Zbirka aforizama, zapisa i meditacija.', 3, FALSE, '010', 'Ivo Andrić');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150009', 'PDF', '2026-05-10', 307, './src/main/resources/knjige/fajlovi/znakovi.pdf');


-- KNJIGA 10: Samo Fizička knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor)
VALUES ('9788617150010', 'Nečista krv', './src/main/resources/knjige/naslovne/krv.jpg', 'Tragična priča o propadanju vranjanskih čorbadžija.', 3, FALSE, '100', 'Borisav Stanković');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150010');

--zanrovi
INSERT INTO zanr (id, name) VALUES
(1, 'Autobiografije'), (2, 'Avanturistički'), (3, 'Biografije'),
(4, 'Domaći pisci'), (5, 'Drama'), (6, 'Epska fantastika'),
(7, 'Film'), (8, 'Horor'), (9, 'Istorijski romani'),
(10, 'Klasici'), (11, 'Komedija'), (12, 'Naučna fantastika'),
(13, 'Poezija'), (14, 'Popularna psihologija'), (15, 'Trileri');

--kategorija clana
INSERT INTO kategorija_clana (idkc, tip_kc, cena_kc) VALUES
(1, 'REGULARNA', 600.00),
(2, 'DECIJA',    300.00),
(3, 'STUDENTSKA',400.00),
(4, 'PENZIONERSKA', 350.00),
(5, 'PORODICNA', 900.00);

-- dobavljaci
INSERT INTO dobavljac (naziv, email, telefon, pib, status)
VALUES ('Delfi Knjižare', 'kontakt@delfi.rs', '011/123-456', '102345678', 'AKTIVAN');

INSERT INTO dobavljac (naziv, email, telefon, pib, status)
VALUES ('Laguna Izdavaštvo', 'podrska@laguna.rs', '011/987-654', '109876543', 'AKTIVAN');

INSERT INTO dobavljac (naziv, email, telefon, pib, status)
VALUES ('Vulkan Izdavaštvo', 'info@vulkani.rs', '011/555-666', '105556661', 'AKTIVAN');


-- 2. knjizare
INSERT INTO knjizara (id, "url_sajta")
VALUES (1, 'https://www.delfi.rs');


-- 3. iydavaci
INSERT INTO izdavac (id)
VALUES (2);

INSERT INTO izdavac (id)
VALUES (3);

-- menadzeri
INSERT INTO korisnik (
    jmbg,
    imeK,
    przK,
    sifraK,
    email,
    tipk,
    bid,
    brojt,
    dat_rodj
) VALUES (
             '1505995800012',
             'Marko',
             'Marković',
             '$2b$12$JFMqeH4J/vrU8rYlH1h7n.Vs0evx4iNNrKzPAltuFhDzQH30.QcbS',  --marko123
             'marko@gmail.com',
             'MENADZER',
             'BIB002',
             '0641234567',
             '1995-05-15'
         );

INSERT INTO korisnik (
    jmbg,
    imeK,
    przK,
    sifraK,
    email,
    tipk,
    bid,
    brojt,
    dat_rodj
) VALUES (
             '2309991800045',
             'Jelena',
             'Jovanović',
             '$2b$12$cr9.vJ0xBLPil79AbvqhSOqWA4/fcKsDH44ktVsCuMYvC.wnq8As6', --jelena123
             'jelena@gmail.com',
             'MENADZER',
             'BIB002',
             '0691234767',
             '1999-05-15'
         );

--ugovori
INSERT INTO ugovor (dobavljac_id, popust, datum_pocetka, datum_isteka, datum_potpisa, rok_isporuke, status)
VALUES (2, 15.0, '2024-06-01', '2025-06-01', '2024-05-28', 14, 'ISTEKAO');

--clan
INSERT INTO korisnik (
    jmbg,
    imeK,
    przK,
    dat_rodj,
    sifraK,
    email,
    brojt,
    tipk,
    bid,
    idkc,
    tip_pretplate,
    putanja_slike
) VALUES (
    '1234567891234',
    'Petar',
    'Petrovic',
    '2026-05-04',
    '$2a$10$MxNR56RCAjGZjfexyE7ThuOVZKtmsHBU5tABCp9T3lylkG4FVJpX.', --testtest
    'nesto@gmail.com',
    '123456789',
    'CLAN',
    'BIB001',
    4,
    'MESECNA',
    NULL
);
--clanarina za Petra
INSERT INTO clanarina (
    datup,
    datisteka,
    datbris,
    is_active,
    nacin_uplate,
    jmbg
) VALUES (
    '2026-05-04',
    '2026-06-23',
    '2026-07-23',
    true,
    'FIZICKI',
    '1234567891234'
);

INSERT INTO korisnik (
    jmbg,
    imeK,
    przK,
    dat_rodj,
    sifraK,
    email,
    brojt,
    tipk,
    bid,
    idkc,
    tip_pretplate,
    putanja_slike
) VALUES (
    '1234567896934',
    'Srdjan',
    'Sancanin',
    '1995-02-25',
    '$2a$10$MxNR56RCAjGZjfexyE7ThuOVZKtmsHBU5tABCp9T3lylkG4FVJpX.',
    'check@not.here',
    '0611636913',
    'BIBLIOTEKAR',
    'BIB001',
    NULL,
    NULL,
    NULL
);

-- =============================================================
-- ELEKTRONSKI ČASOPISI
-- =============================================================

-- Časopis 1 — izdavač: Laguna (id=2)
INSERT INTO elektronski_casopis (issn, naziv_ec, oblast_ec, opis_ec, jezik_ec, ucestalost_izdavanja_ec, putanja_slike_ec, id_izdavaca_ec)
VALUES ('00278232', 'Letopis Matice srpske', 'Književnost', 'Najstariji književni časopis na srpskom jeziku.', 'Srpski', 'Mesečno', '/casopisi/letopis.jpg', 2);

-- Časopis 2 — izdavač: Vulkan (id=3)
INSERT INTO elektronski_casopis (issn, naziv_ec, oblast_ec, opis_ec, jezik_ec, ucestalost_izdavanja_ec, putanja_slike_ec, id_izdavaca_ec)
VALUES ('18207995', 'Savremena tehnika', 'Tehnika i tehnologija', 'Stručni časopis iz oblasti inženjerstva i primenjenih nauka.', 'Srpski', 'Kvartalno', '/casopisi/savremena_tehnika.jpg', 3);


-- =============================================================
-- BROJEVI ČASOPISA  (issn, broj_izdanja)
-- =============================================================

-- Letopis Matice srpske — 3 broja
INSERT INTO broj_casopisa (issn, broj_izdanja, volumen_bc, datum_izdavanja_bc, putanja_dokumenta_bc)
VALUES ('00278232', 1, 501, '2026-01-01', '/casopisi/letopis/2026_1.pdf');

INSERT INTO broj_casopisa (issn, broj_izdanja, volumen_bc, datum_izdavanja_bc, putanja_dokumenta_bc)
VALUES ('00278232', 2, 501, '2026-02-01', '/casopisi/letopis/2026_2.pdf');

INSERT INTO broj_casopisa (issn, broj_izdanja, volumen_bc, datum_izdavanja_bc, putanja_dokumenta_bc)
VALUES ('00278232', 3, 501, '2026-03-01', '/casopisi/letopis/2026_3.pdf');

-- Savremena tehnika — 2 broja
INSERT INTO broj_casopisa (issn, broj_izdanja, volumen_bc, datum_izdavanja_bc, putanja_dokumenta_bc)
VALUES ('18207995', 1, 78, '2026-01-15', '/casopisi/savremena_tehnika/2026_1.pdf');

INSERT INTO broj_casopisa (issn, broj_izdanja, volumen_bc, datum_izdavanja_bc, putanja_dokumenta_bc)
VALUES ('18207995', 2, 78, '2026-04-15', '/casopisi/savremena_tehnika/2026_2.pdf');


-- =============================================================
-- ELEKTRONSKE BAZE PODATAKA
-- =============================================================

-- Baza 1 — izdavač: Laguna (id=2)
INSERT INTO elektronska_baza_podataka (naziv_ebp, oblast_ebp, opis_ebp, licenca_ebp, id_izdavaca_ebp)
VALUES ('SrpskaBib Online', 'Književnost i humanistika', 'Digitalna baza srpske književne baštine i naučnih radova iz humanistike.', 'CC BY-NC 4.0', 2);

-- Baza 2 — izdavač: Vulkan (id=3)
INSERT INTO elektronska_baza_podataka (naziv_ebp, oblast_ebp, opis_ebp, licenca_ebp, id_izdavaca_ebp)
VALUES ('TehnoRef', 'Inženjerstvo i tehnika', 'Referentna baza tehničke dokumentacije, standarda i stručnih publikacija.', 'Komercijalna', 3);


-- =============================================================
-- CITANJE E-KNJIGE  (clan: Petar, jmbg='1234567891234')
-- =============================================================

-- Čita "Prokletu avliju" (isbn=9788617150002) — u toku
INSERT INTO citanje_eknjige (jmbg_clana, isbn_eknjige, datum_pocetka_ck, trenutna_stranica_ck, datum_poslednjeg_pristupa_ck, datum_zavrsetka_ck, status_citanja_ck)
VALUES ('1234567891234', '9788617150002', '2026-05-01', 45, '2026-05-10', NULL, 'U_TOKU');

-- Čita "Gorski vijenac" (isbn=9788617150004) — završeno
INSERT INTO citanje_eknjige (jmbg_clana, isbn_eknjige, datum_pocetka_ck, trenutna_stranica_ck, datum_poslednjeg_pristupa_ck, datum_zavrsetka_ck, status_citanja_ck)
VALUES ('1234567891234', '9788617150004', '2026-04-01', 215, '2026-04-18', '2026-04-18', 'ZAVRSENO');

-- Ponovo čita "Gorski vijenac" (drugi put — drugačiji datum_pocetka)
INSERT INTO citanje_eknjige (jmbg_clana, isbn_eknjige, datum_pocetka_ck, trenutna_stranica_ck, datum_poslednjeg_pristupa_ck, datum_zavrsetka_ck, status_citanja_ck)
VALUES ('1234567891234', '9788617150004', '2026-05-05', 80, '2026-05-12', NULL, 'U_TOKU');

-- Napustio "Znakove pored puta" (isbn=9788617150009)
INSERT INTO citanje_eknjige (jmbg_clana, isbn_eknjige, datum_pocetka_ck, trenutna_stranica_ck, datum_poslednjeg_pristupa_ck, datum_zavrsetka_ck, status_citanja_ck)
VALUES ('1234567891234', '9788617150009', '2026-03-10', 22, '2026-03-15', NULL, 'NAPUSTENO');


-- =============================================================
-- SLUŠANJE AUDIO KNJIGE  (clan: Petar, jmbg='1234567891234')
-- =============================================================

-- Sluša "Seobe" (isbn=9788617150003) — u toku
INSERT INTO slusanje_audio_knjige (jmbg_clana, isbn_audio_knjige, datum_pocetka_sak, trenutna_sekunda_sak, datum_poslednjeg_pristupa_sak, datum_zavrsetka_sak, status_slusanja_sak)
VALUES ('1234567891234', '9788617150003', '2026-05-08', 3600, '2026-05-11', NULL, 'U_TOKU');

-- Odslušao "Gorski vijenac" (isbn=9788617150004) — završeno
INSERT INTO slusanje_audio_knjige (jmbg_clana, isbn_audio_knjige, datum_pocetka_sak, trenutna_sekunda_sak, datum_poslednjeg_pristupa_sak, datum_zavrsetka_sak, status_slusanja_sak)
VALUES ('1234567891234', '9788617150004', '2026-04-20', 14400, '2026-04-25', '2026-04-25', 'ZAVRSENO');

-- Ponovo sluša "Gorski vijenac" (drugi put)
INSERT INTO slusanje_audio_knjige (jmbg_clana, isbn_audio_knjige, datum_pocetka_sak, trenutna_sekunda_sak, datum_poslednjeg_pristupa_sak, datum_zavrsetka_sak, status_slusanja_sak)
VALUES ('1234567891234', '9788617150004', '2026-05-15', 1200, '2026-05-15', NULL, 'U_TOKU');

-- Napustio "Tvrdavu" (isbn=9788617150007)
INSERT INTO slusanje_audio_knjige (jmbg_clana, isbn_audio_knjige, datum_pocetka_sak, trenutna_sekunda_sak, datum_poslednjeg_pristupa_sak, datum_zavrsetka_sak, status_slusanja_sak)
VALUES ('1234567891234', '9788617150007', '2026-03-01', 900, '2026-03-02', NULL, 'NAPUSTENO');


-- =============================================================
-- PREUZIMANJE BAZE PODATAKA  (clan: Petar, jmbg='1234567891234')
-- =============================================================

-- Preuzeo "SrpskaBib Online" (id=1) — dva puta
INSERT INTO preuzimanje_baze_podataka (jmbg_clana, id_baze, datum_preuzimanja_pbp)
VALUES ('1234567891234', 1, '2026-04-10');

INSERT INTO preuzimanje_baze_podataka (jmbg_clana, id_baze, datum_preuzimanja_pbp)
VALUES ('1234567891234', 1, '2026-05-10');

-- Preuzeo "TehnoRef" (id=2) — jednom
INSERT INTO preuzimanje_baze_podataka (jmbg_clana, id_baze, datum_preuzimanja_pbp)
VALUES ('1234567891234', 2, '2026-05-01');


-- =============================================================
-- PRISTUP BROJU ČASOPISA  (clan: Petar, jmbg='1234567891234')
-- =============================================================

-- Pristupio Letopisu br.1 — dva puta
INSERT INTO pristup_broju_casopisa (jmbg_clana, issn_casopisa, broj_izdanja, datum_pristupanja_pbc)
VALUES ('1234567891234', '00278232', 1, '2026-02-05');

INSERT INTO pristup_broju_casopisa (jmbg_clana, issn_casopisa, broj_izdanja, datum_pristupanja_pbc)
VALUES ('1234567891234', '00278232', 1, '2026-03-10');

-- Pristupio Letopisu br.2
INSERT INTO pristup_broju_casopisa (jmbg_clana, issn_casopisa, broj_izdanja, datum_pristupanja_pbc)
VALUES ('1234567891234', '00278232', 2, '2026-03-15');

-- Pristupio Savremena tehnika br.1
INSERT INTO pristup_broju_casopisa (jmbg_clana, issn_casopisa, broj_izdanja, datum_pristupanja_pbc)
VALUES ('1234567891234', '18207995', 1, '2026-02-20');
