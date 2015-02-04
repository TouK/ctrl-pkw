ctrl-pkw
========

Idea tego proejektu jest taka, żeby zbierać informacje z papierowych protokołów,
które w dniu wyborów będą wywieszone w lokalach wyborczych i pokazać wyniki wyborów
wcześniej niż PKW policzy to w Office-ie. 

Udostępnimy aplikację mobilną. 10 maja trochę po 20:00 użytkownicy po jej odpaleniu
otrzymają informację o kilku najbliższych jego lokalizacji lokalach wyborczych, 
z których nie zostały jeszcze przesłane do naszego systemu wyniki cząstkowe.
Zakładamy, że znajdzie grupa (niewielka, bo tylko 26k) zapaleńcóœ, którzy wezmą 
w tym momencie psa na spacer i pójdą spisać protokół i zrobić mu telefonem zdjęcie.
My to wszystko przeliczymy i udostępniamy stronę z wynikiem.

**ProtocolsGatheringIT** to test, który symuluje wybory sprzed 5 lat.

**WardGeolocalizationIT** to przykład wyszukania najbliższych lokali wyborczych,
przy czym teraz ich współrzędne są na razie losowane. Docelowo trzeba będzie 
je chyba odszukać tu http://download.geofabrik.de/europe/poland.html ???

