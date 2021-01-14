# SPDBApp
## 1. Funkcjonalność aplikacji
Aplikacja służy do wyznaczania trasy pomiędzy dwoma zadanymi punktami na mapie z terenu Polski z uwzględnieniem typu miejsca do odwiedzenia w trakcie podróży. Użytkownik może dodatkowo sprecyzować odległości pomiędzy odwiedzanymi punktami oraz minimalną odległość pierwszego/ostatniego POI od początku/końca trasy.Aplikacja zwraca obliczoną trasę w postaci ścieżku narysowanej na mapie. W przypadku nieistnienia połączenia spełniającego zadane kryteria aplikacja zwraca informację o nieznalezieniu trasy.

## 2. Baza danych
Jako źródło danych przestrzennych wykorzystano OpenStreetMap. Do ich przechowywania użyto bazę danych PostgreSQL z rozszerzeniami postgis i pgrouting, hostowaną na serwerze RDS Amazon Web Services. Do importu trasowalnej sieci dróg użyto narzędzie [osm2po](https://osm2po.de/), zaś do importu konkretnych punktów zainteresowania (poi) dane pobrane z API [overpass turbo](https://overpass-turbo.eu/), obrobione narzędziem [osmconvert](https://wiki.openstreetmap.org/wiki/Osmconvert) i wgrane z użyciem [osm2pgsql](https://wiki.openstreetmap.org/wiki/Osm2pgsql).

## 3. Wybór parametrów przez użytkownika

Punkt początkowy i końcowy mogą zostać wybrane przez użytkownika poprzez zaznaczenie ich na mapie, bądź wpisanie nazwy miejscowości w odpowiednim oknie. W przypadku, gdy w Polsce występuje więcej niż jedna miejscowość o zadanej nazwie, użytkownik ma możliwość wyboru odpowiedniego miejsca z wyświetlonej listy. Kategoria odwiedzanych obiektów POI jest możliwa do zaznaczenia na liście, natomiast pozostałe parametry określające odległości miedzy POI użytkownik wpisuje z wskazanych do tego miejscach.

## 4. Architektura aplikacji

Aplikacja została napisana zgodnie z wzorcem architektonicznym MVC. Część widoku odpowiedzialna za przyjmowanie od użytkownika parametrów została zrealizowana za pomocą biblioteki Swing, natomiast do wyświetalania mapy, trasy i POI użyto biblioteki JXMapViewer2.

Aplikacja stworzona została w dwuwarstwowej archtekturze klient-serwer. Po stronie klienta zrealizowano pobieranie danych od użytkownika oraz wyświetlanie wyników otrzymanych z serwera. Serwer, w postaci bazy danych odpowiada za odnajdywanie trasy i POI.

W programie zostały wyszczególnione następujące klasy:
-AppWindow- klasa odpowiedzialna za przyjmowanie i wyświetlanie danych. Posiada kontroler, za pomocą którego wywołuje funkcje obiektu klasy QueryExecuter.
-QueryExecuter- klasa odpowiedzialna za komunikację z bazą danych. Na podstawie parametrów otrzymanych od klasy AppWindow generuje zapytania, które wykonuje na bazie. Otrzymane wyniki przekazuje do AppWindow
-Route- klasa przechowująca listę punktów składających się na ścieżkę oraz informacje o czasie i długości podróży
-POI- klasa przechowująca nazwę oraz współrzędne POI.

## 5. Algorytm wyszukiwania trasy

Przy uruchomieniau aplikacji zostaje utworzona tymczasowa tabela przechowująca drogi. Dla przyspieszenia obliczeń przyjęte zostało założenie, że po Polsce najszybciej przemieszcza się drogami krajowymi i autostradami, a mniejsze drogi służą jedynie do dojechania do większych ulic. Do tabeli wstawiane sąautostrady i drogi krajowe znadujące się najbliżej odcinka pomiędzy punktem początkowym i końcowym oraz drogi każdego rodzaju znajdujące się w pobliżu punku począkowego i końcowego. Dla tak wyznaczonego zbiory wywoływany zostaje algorytm A* odnajdujący najkrótszą ścieżkę pomiędzy punktem początkowym i końcowym. W celu skrócenia czasu zapytania na tabeli został założony indeks przestrzenny (!!!!Zbyszek potwierdź!!!!). Zastosowanie wymienionych optymalizacji pozwoliło skrócić czas zapytania z trzech minut do kilkunastu sekund.

```
SELECT id, ST_AsText(geom_way) as way, geom_way  FROM pgr_astar(
    'select * from routing',
    (SELECT source FROM hh_2po_4pgr
    ORDER BY ST_Distance(
        ST_StartPoint(geom_way),
        ST_SetSRID(ST_MakePoint(18.0096173286438,50.734526693101444), 4326),
        true
   ) ASC limit 1),
	(SELECT source FROM hh_2po_4pgr
    ORDER BY ST_Distance(
        ST_StartPoint(geom_way),
        ST_SetSRID(ST_MakePoint(18.003673553466797,50.72320566967355 ), 4326),
        true
   ) ASC limit 1),
	true)
		as waypoints
JOIN hh_2po_4pgr rd ON waypoints.edge = rd.id ORDER BY path_seq;
```

## 5. Algorytm wyszukiwania POI

Na znalezionej trasie wyznaczone zostają punkty, spełniające zadane przez użytkownika kryteria. Następnie wyszukiwane zostają POI znajdujące się najbliżej zaznaczonych punktów. Dla zwróconych POI do tymczasowej tabeli dróg wstawiane są drogi każdego rodzaju znajdujące się najbliżej nich. Następnie, za pomocą algorytmu A* zostaje odnaleziona najkrótsza trasa przechodząca przez odnalezione punkty.

## 6. Analiza przykładowych wyników (nie wiem czy do chcemy w dokumentacji, czy tylko pokazujemy na żywo)


## 7. Podsumowanie
Utworzona aplikacja spełnia postawione przed nią wymagania funkcjonalne. Dzięki wykorzystaniu indeksów przestrzennych oraz zapisywaniu danych do tabeli tymczasowej zwraca ona wynik w zadowalającym czasie. Zastosowany algorytm wyszukiwana POI zwraca punkty, których odwiedzenie w minimalnym stopiu wydłuża pierwotną podróż.

## 8. Podział pracy w zespole
To wstawiamy w dokumentacji?
