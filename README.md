# Dominik-adocha
# Aktualizacja projektu w Android Studio

Poniższe kroki pokazują, jak pobrać najnowsze zmiany z GitHuba do projektu w Android Studio.

1. **Otwórz projekt w Android Studio.**
   - Uruchom Android Studio i załaduj projekt `Dominik-adocha` (Plik → Otwórz / File → Open).
2. **Sprawdź połączenie z Git.**
   - W prawym dolnym rogu upewnij się, że widzisz nazwę gałęzi (np. `main`).
   - Jeśli pojawi się komunikat o konieczności autoryzacji GitHuba, zaloguj się.
3. **Synchronizuj repozytorium.**
   - Przejdź do menu **VCS → Git → Pull...** (lub kliknij ikonę strzałki w dół na pasku narzędzi VCS).
   - W oknie "Pull" pozostaw domyślne ustawienia (`Remote: origin`, `Branch: main`) i naciśnij **Pull**.
4. **Poczekaj na pobranie zmian.**
   - Android Studio pokaże postęp w dolnym pasku. Po zakończeniu zobaczysz komunikat "Pull successful".
5. **Odśwież projekt.**
   - Gdy Gradle zakończy synchronizację, możesz uruchomić aplikację ponownie (`Run` ▶️), aby sprawdzić nowe funkcje.

> 💡 Jeśli pojawią się konflikty, Android Studio wyświetli okno rozwiązania konfliktów. Wybierz odpowiednie wersje plików i potwierdź, aby dokończyć `pull`.
