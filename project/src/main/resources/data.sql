-- noinspection SqlNoDataSourceInspectionForFile

-- KNJIGA 1: Samo Fizička knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150001', 'Na Drini ćuprija', '/naslovne/cuprija.jpg', 'Roman o istoriji višegradskog mosta.');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150001');


-- KNJIGA 2: Samo E-Knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150002', 'Prokleta avlija', '/naslovne/avlija.jpg', 'Priča o zatvorenicima u istanbulskom zatvoru.');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150002', 'PDF', '2026-01-15', 120, '/fajlovi/avlija.pdf');


-- KNJIGA 3: Samo Audio knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150003', 'Seobe', '/naslovne/seobe.jpg', 'Istorijski roman o seobama Srba.');

INSERT INTO audio_knjiga (isbn, trajanje_sek_ak, format_ak, datum_dodavanja_ak, putanja_ak)
VALUES ('9788617150003', 28800, 'MP3', '2026-02-10', '/audio/seobe.mp3');


-- KNJIGA 4: Sva tri formata
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150004', 'Gorski vijenac', '/naslovne/gorski.jpg', 'Poem epske fantastike i filozofije.');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150004');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150004', 'EPUB', '2026-03-05', 215, '/fajlovi/gorski.epub');

INSERT INTO audio_knjiga (isbn, trajanje_sek_ak, format_ak, datum_dodavanja_ak, putanja_ak)
VALUES ('9788617150004', 14400, 'M4A', '2026-03-01', '/audio/gorski.m4a');


-- KNJIGA 5: Kombinacija: Fizička i E-Knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150005', 'Hazarski rečnik', '/naslovne/hazari.jpg', 'Roman leksikon u ženskom i muškom primerku.');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150005');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150005', 'PDF', '2026-04-12', 350, '/fajlovi/hazarski_recnik.pdf');


-- KNJIGA 6: Kombinacija: E-Knjiga i Audio knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150006', 'Koreni', '/naslovne/koreni.jpg', 'Roman o porodici Katić.');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150006', 'EPUB', '2026-04-20', 280, '/fajlovi/koreni.epub');

INSERT INTO audio_knjiga (isbn, trajanje_sek_ak, format_ak, datum_dodavanja_ak, putanja_ak)
VALUES ('9788617150006', 21600, 'MP3', '2026-04-22', '/audio/koreni.mp3');


-- KNJIGA 7: Kombinacija: Fizička i Audio knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150007', 'Tvrđava', '/naslovne/tvrdjava.jpg', 'Priča o Ahmetu Šabi i njegovom mjestu u svijetu.');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150007');

INSERT INTO audio_knjiga (isbn, trajanje_sek_ak, format_ak, datum_dodavanja_ak, putanja_ak)
VALUES ('9788617150007', 32400, 'MP3', '2026-05-01', '/audio/tvrdjava.mp3');


-- KNJIGA 8: Samo Fizička knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150008', 'Derviš i smrt', '/naslovne/dervis.jpg', 'Psihološko-filozofski roman Meše Selimovića.');

INSERT INTO fizicka_knjiga (isbn)
VALUES ('9788617150008');


-- KNJIGA 9: Samo E-Knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150009', 'Znakovi pored puta', '/naslovne/znakovi.jpg', 'Zbirka aforizama, zapisa i meditacija.');

INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
VALUES ('9788617150009', 'PDF', '2026-05-10', 400, '/fajlovi/znakovi.pdf');


-- KNJIGA 10: Samo Fizička knjiga
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis)
VALUES ('9788617150010', 'Nečista krv', '/naslovne/krv.jpg', 'Tragična priča o propadanju vranjanskih čorbadžija.');

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

--biblioteke
INSERT INTO biblioteka (bid, name, ziro_rb) VALUES
('BIB001', 'Gradska biblioteka Novi Sad', '840-123456-78'),
('BIB002', 'Biblioteka Matica srpska','840-234567-89'),
('BIB003', 'Biblioteka Zmaj','840-345678-90');


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