# SPDBApp
##### Projekt SPDB - Przestrzenne Bazy Danych
###### Zbigniew Kaczyński
###### Paweł Krysztofik
###### Wojciech Wolny

## 1. Treść Projektu
Naszym projektem był projekt numer 16 o treści:

Zadane jest miejsce początkowe i miejsce docelowe podróży. Napisać aplikację, która wskaże miejsca do odwiedzenie (dowolnie wybrane tzw. punkty zainteresowanie -POI) oraz wyznaczy trasę przejazdu przy spełnieniu ograniczeń dotyczącej trasy: wydłużenia czasu przejazdu, długości trasy oraz okresu w czasie podróży, w którym dany punkt ma być odwiedzony (np. po godzinie od rozpoczęcia podróży).

Aplikacja powinna zawierać autonomiczny moduł do pokazania wyznaczonej trasy na mapie. Do napisania aplikacji należy wykorzystać dostępne serwisy oferujące dane przestrzenne (np. serwis OpenStreetMap http://www.openstreetmap.org/).

## 2. Funkcjonalność aplikacji
Aplikacja służy do wyznaczania trasy pomiędzy dwoma zadanymi punktami na mapie z terenu Polski z uwzględnieniem typu miejsca do odwiedzenia w trakcie podróży. Użytkownik może dodatkowo sprecyzować odległości pomiędzy odwiedzanymi punktami oraz minimalną odległość pierwszego/ostatniego POI od początku/końca trasy. Aplikacja zwraca obliczoną trasę w postaci ścieżku narysowanej na mapie. W przypadku nieistnienia połączenia spełniającego zadane kryteria aplikacja zwraca informację o nieznalezieniu trasy.

## 3. Baza danych
Jako źródło danych przestrzennych wykorzystano OpenStreetMap. Do ich przechowywania użyto bazę danych PostgreSQL z rozszerzeniami postgis i pgrouting, hostowaną na serwerze RDS Amazon Web Services. Do importu trasowalnej sieci dróg użyto narzędzie [osm2po](https://osm2po.de/), zaś do importu konkretnych punktów zainteresowania (poi) dane pobrane z API [overpass turbo](https://overpass-turbo.eu/), obrobione narzędziem [osmconvert](https://wiki.openstreetmap.org/wiki/Osmconvert) i wgrane z użyciem [osm2pgsql](https://wiki.openstreetmap.org/wiki/Osm2pgsql). W naszej bazie danych posiadamy informacje o systemie drogowym w Polsce, informację o punktach zainteresowania - POI z listy "toalety, restauracje, fast foody, stacje tankowania oraz stacje ładowania" oraz informacje o miejscowościach.

## 4. Wykorzystane narzędzia i biblioteki
+ język programowania Java
+ IntelliJ Idea IDE
+ Maven - system do zarządzania bibliotekami w projekcie
+ JxMapViewr - biblioteka do wyświetlania mapy
+ PostgreSQL 42.2.18 - biblioteka do obsługi przestrzennej bazy danych
+ pgAdmin - narzędzie do połączenia z serwerem bazy danych
+ AmazonWebServices - dostawca usług chmurowych, które wykorzystaliśmy do postawienia przestrzennej bazy danych
+ OpenStreetMap - serwis udostępniający dane przestrzenne
+ git - system kontroli wersji
+ github - https://github.com/pkryszto/SPDBApp

## 5. Wybór parametrów przez użytkownika

Punkt początkowy i końcowy mogą zostać wybrane przez użytkownika poprzez zaznaczenie ich na mapie, bądź wpisanie nazwy miejscowości w odpowiednim oknie. W przypadku, gdy w Polsce występuje więcej niż jedna miejscowość o zadanej nazwie, użytkownik ma możliwość wyboru odpowiedniego miejsca z wyświetlonej listy. Kategoria odwiedzanych obiektów POI jest możliwa do zaznaczenia na liście, natomiast pozostałe parametry określające odległości między POI użytkownik wpisuje z wskazanych do tego miejscach.

## 6. Architektura aplikacji

Aplikacja została napisana zgodnie z wzorcem architektonicznym MVC. Część widoku odpowiedzialna za przyjmowanie od użytkownika parametrów została zrealizowana za pomocą biblioteki Swing, natomiast do wyświetlania mapy, trasy i POI użyto biblioteki JXMapViewer2.

Aplikacja stworzona została w dwuwarstwowej architekturze klient-serwer. Po stronie klienta zrealizowano pobieranie danych od użytkownika oraz wyświetlanie wyników otrzymanych z serwera. Serwer, w postaci bazy danych odpowiada za odnajdywanie trasy i POI.

W programie zostały wyszczególnione następujące klasy:
+ AppWindow - klasa odpowiedzialna za przyjmowanie i wyświetlanie danych. Posiada kontroler, za pomocą którego wywołuje funkcje obiektu klasy QueryExecuter.
+ QueryExecuter - klasa odpowiedzialna za komunikację z bazą danych. Na podstawie parametrów otrzymanych od klasy AppWindow generuje zapytania, które wykonuje na bazie. Otrzymane wyniki przekazuje do AppWindow
+ Route - klasa przechowująca listę punktów składających się na ścieżkę oraz informacje o czasie i długości podróży
+ POI - klasa przechowująca nazwę oraz współrzędne POI.
+ Address - klasa służąca do przedstawienia informacji o miejscowości wraz z informacją o jej współrzędnych geograficznych.

## 7. Algorytm wyszukiwania trasy

Przy uruchomieniu aplikacji zostaje utworzona tymczasowa tabela przechowująca drogi. Dla przyspieszenia obliczeń przyjęte zostało założenie, że po Polsce najszybciej przemieszcza się drogami krajowymi i autostradami, a mniejsze drogi służą jedynie do dojechania do większych ulic. Do tabeli wstawiane są autostrady i drogi krajowe znajdujące się najbliżej odcinka pomiędzy punktem początkowym i końcowym oraz drogi każdego rodzaju znajdujące się w pobliżu punku początkowego i końcowego. Dla tak wyznaczonego zbioru wywoływany zostaje algorytm A* odnajdujący najkrótszą ścieżkę pomiędzy punktem początkowym i końcowym. W celu skrócenia czasu zapytania na tabeli został założony indeks przestrzenny. Zastosowanie wymienionych optymalizacji pozwoliło skrócić czas zapytania z dwóch minut do kilku sekund.

```
SELECT * FROM pgr_astar(
    '(SELECT * from routing2
ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(20,53), 4326)
LIMIT 10000)
UNION (SELECT * from routing2
ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(20,50), 4326)
LIMIT 10000)
UNION (select * from fast_ways
ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(20,51.5), 4326)
LIMIT 30000)',
    (SELECT source FROM ways 
		ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(20,50), 4326)
		LIMIT 1),
	(SELECT source FROM ways 
		ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(20,53), 4326)
		LIMIT 1),
	true
) as waypoints
INNER JOIN ways rd ON waypoints.edge = rd.id;
```

## 8. Algorytm wyszukiwania POI

Na znalezionej trasie wyznaczone zostają punkty, spełniające zadane przez użytkownika kryteria. Następnie wyszukiwane zostają POI znajdujące się najbliżej zaznaczonych punktów. Dla zwróconych POI do tymczasowej tabeli dróg wstawiane są drogi każdego rodzaju znajdujące się najbliżej nich. Następnie, za pomocą algorytmu A* zostaje odnaleziona najkrótsza trasa przechodząca przez odnalezione punkty.

Przykładowe zapytanie zwracające stację benzynową najbliższą danym współrzędnym geograficznym.
```
SELECT name, way, ST_AsText(way) FROM pois where amenity='fuel'
		ORDER BY way <-> ST_SetSRID(ST_MakePoint(20,53), 4326)
		LIMIT 1
```

## 9. Podsumowanie
Utworzona aplikacja spełnia postawione przed nią wymagania funkcjonalne. Dzięki wykorzystaniu indeksów przestrzennych oraz zapisywaniu danych do tabeli tymczasowej zwraca ona wynik w zadowalającym czasie. Zastosowany algorytm wyszukiwana POI zwraca punkty, których odwiedzenie w minimalnym stopniu wydłuża pierwotną podróż.

