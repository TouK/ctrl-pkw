ctrl-pkw
========

Idea tego projektu jest taka, żeby zbierać informacje z papierowych protokołów,
które w dniu wyborów będą wywieszone w lokalach wyborczych i pokazać wyniki wyborów
wcześniej niż PKW policzy to w Office-ie. 

Udostępnimy aplikację mobilną. 10 maja trochę po 20:00 użytkownicy po jej odpaleniu
otrzymają informację o kilku najbliższych jego lokalizacji lokalach wyborczych, 
z których nie zostały jeszcze przesłane do naszego systemu wyniki cząstkowe.
Zakładamy, że znajdzie grupa (niewielka, bo tylko 26k) zapaleńców, którzy wezmą 
w tym momencie psa na spacer i pójdą spisać protokół i zrobić mu telefonem zdjęcie.
My to wszystko przeliczymy i udostępniamy stronę z wynikiem.

**ProtocolsGatheringIT** to test, który symuluje wybory sprzed 5 lat.

**WardGeolocalizationIT** to przykład wyszukania najbliższych lokali wyborczych.
W tej chwili w aplikacji wpisane są współrzędne prawie 80% lokali wyborczych.

Do uruchomienia aplikacji potrzebna jest zainstalowana baza Apache Cassandra.
Na Ubuntu instaluje się ja tak:
http://www.datastax.com/documentation/cassandra/2.1/cassandra/install/installDeb_t.html
