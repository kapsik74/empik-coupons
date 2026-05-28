| | |
|---|---|
| **Autor** | Michał Kaps • michal.kaps74@gmail.com |
| **Projekt** | Serwis do tworzenia i zarządzania kuponami rabatowymi |
| **Uruchamianie** | Maven 3.9 + PostgreSQL 17 (Docker image) |
 	
	
1. Zadanie wykonane w technologii:
   - Java 21, by wykorzystać wirtualne wątki
   - Spring Boot 4.0.6 - najnowsza wersja
   - Hiberante 6.x
   - baza danych PostgreSQL 17, ładowane z dockera
2. Do zarządzania zmianami oraz migracją schematu bazy danych użyłem liquibase i standardowego pliku db.changelog-master.yaml do zarządzania changesetami
3. Zamiast reaktywnego podejścia użyłem virtual threads oraz serwera Tomcat. 
4. Klasy z adnotacją @Service opakowane są jeszcze Lombokiem aby wyeliminować boilerplate code
5. Do obsługi błędów użyłem @ControllerAdvice, który łapie błędy z różnych miejsc aplikacji
6. Testy z wykorzystaniem:
   - testcontainers 2.x
   - JUnit 5
   - Mockito
   - AssertJ
7. Mając JUnit oraz AssertJ zastosowałem testy BDD (given, when, then)
