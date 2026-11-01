# BlackServ Gallery — wybór całych dni w albumach

Wersja testowa: 3.0.0-blackserv.1

## Zakres

- Odrębna instalacja Android: `eu.blackserv.gallery` / **BlackServ Gallery**.
- W widoku grupowanym, także w albumie **Screenshots**, ikona przy nagłówku daty zaznacza wszystkie zdjęcia i filmy z danego dnia.
- Kolejne daty dodają się do zaznaczenia. Ponowne wybranie całego zaznaczonego dnia usuwa z zaznaczenia tylko jego elementy.
- Ikona kosza przenosi wybrane elementy do Kosza Androida z potwierdzeniem systemowym. Więcej niż 2000 pozycji przetwarzane jest kolejno; anulowanie zatrzymuje pozostałe.
- Uwzględniany jest bieżący album, filtr, sposób sortowania według daty i lokalna strefa czasu (również dni zmiany czasu).

## Kod źródłowy i build

Ta gałąź przechowuje poprawkę [patches/blackserv-gallery-day-selection.patch](patches/blackserv-gallery-day-selection.patch) względem publicznego `Bn5prS/Photon_Gallery` (commit `382c7a28fbda1907d76d12a43f604ed6d50b181e`).

Workflow [build-blackserv-gallery.yml](.github/workflows/build-blackserv-gallery.yml) pobiera wskazaną wersję kodu źródłowego, nakłada poprawkę, uruchamia testy i buduje APK. Po sukcesie publikuje kompletny kod jako nową gałąź `source/blackserv-gallery`. Nie nadpisuje `main`.

APK z obecnego workflow jest **testowa, podpisywana kluczem debug**. Produkcyjne aktualizacje wymagają własnego trwałego klucza podpisu. Nie instaluj jej jako zamiennika oryginalnej Photon Gallery — ma inny identyfikator aplikacji.

## Autorstwo

Projekt bazuje na **Photon Gallery** autorstwa **Bn5prS**. Zachowano kod i warunki licencji GPLv3 wraz z informacją o autorze. Mechanizm chmury Google z OPPO Photos pozostaje poza tym zakresem; będzie rozpatrywany odrębnie.
