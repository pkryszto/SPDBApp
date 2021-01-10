# SPDBApp
## 1. Funkcjonalność aplikacji
Aplikacja służy do wyznaczania trasy pomiędzy dwoma zadanymi punktami na mapie z terenu Polski z uwzględnieniem typu miejsca do odwiedzenia w trakcie podróży. Użytkownik może dodatkowo sprecyzować parametry trasy ustawiając maksymalne wydłużenia czasu i długości przejazdu (np. o 20 minut), minimalny oraz maksymalny czas, po którym ma zostać odwiedzony punkt (np. po godzinie od rozpoczęcia podróży) oraz przedział trasy, w którym na zostać odwiedzony punkt (np. po 100 kilometrach od wyjazdu, ale 50 kilometrów od celu). Aplikacja zwraca obliczoną trasę w postaci ścieżkę narysowaną na mapie lub informację o nieznalezieniu trasy spełniającej zadane warunki. 

## 2. Baza danych
Jako źródło danych przestrzenny wykorzystano OpenStreetMap. Do ich przechowywania użyto bazę danych PostgreSQL z rozszerzeniami postgis i pgrouting, hostowaną na serwerze RDS Amazon Web Services. Do importu trasowalnej sieci dróg użyto narzędzie [osm2po](https://osm2po.de/), zaś do importu konkretnych punktów zainteresowania (poi) dane pobrane z API [overpass turbo](https://overpass-turbo.eu/), obrobione narzędziem [osmconvert](https://wiki.openstreetmap.org/wiki/Osmconvert) i wgrane z użyciem [osm2pgsql](https://wiki.openstreetmap.org/wiki/Osm2pgsql).

## 3. Architektura aplikacji


## 4. Algorytm wyszukiwania trasy

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

## 5. Analiza przykładowych wyników

## 6. Podsumowanie

## 7. Podział pracy w zespole
