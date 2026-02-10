package com.carrental.backend.service;

import com.carrental.backend.model.Rental;
import com.carrental.backend.model.Customer;
import com.carrental.backend.model.Car;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class PdfGenerator {

    public byte[] generateShortTermContract(Rental rental) {

        // ✅ LICZYMY DNI
        long days = ChronoUnit.DAYS.between(
                rental.getStartDate(),
                rental.getEndDate()
        );
        if (days == 0) days = 1;

        // ✅ LICZYMY CENĘ CAŁKOWITĄ
        BigDecimal totalPrice =
                rental.getPricePerDay().multiply(BigDecimal.valueOf(days));

        // ✅ BUDUJEMY HTML
        String html = buildHtml(rental, totalPrice, days);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(
                    () -> getClass()
                            .getResourceAsStream("/fonts/DejaVuSans.ttf"),
                    "DejaVu Sans");
            builder.withHtmlContent(html, "");
            builder.toStream(out);
            builder.run();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Błąd generowania PDF", e);
        }
    }

    private String buildHtml(Rental rental, BigDecimal totalPrice, long days) {

        Customer c = rental.getCustomer();
        Car car = rental.getCar();

        StringBuilder sb = new StringBuilder();
        sb.append("""    
<!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8"/>
                <style>
                body {
            font-family: 'DejaVu Sans';
            font-size: 11px;
            line-height: 1.4;
            padding: 20px;
        }
        h1 {
            text-align: center;
            font-size: 16px;
            margin-bottom: 20px;
            text-decoration: underline;
        }
        h2 {
            font-size: 13px;
            margin-top: 18px;
            margin-bottom: 8px;
        }
        h3 {
                                font-size: 12px;
                                margin-top: 15px;
                                margin-bottom: 6px;
                                font-weight: bold;
                            }
        p {
            margin: 4px 0;
        }
    .section {
            margin-top: 15px;
        }
    .underline {
            border-bottom: 1px solid #000;
            display: inline-block;
            min-width: 200px;
            padding: 2px 0;
        }
    .signature-block {
            margin-top: 60px;
            display: flex;
            justify-content: space-between;
            page-break-inside: avoid;
        }
    .signature {
            width: 45%;
            text-align: center;
        }
    .regulations {
            font-size: 10px;
            margin-top: 30px;
            page-break-before: always;
        }
        .page-break {
                                page-break-before: always;
                                break-before: page;
                            }
</style>
                </head>
                <body>
                """);
        
        sb.append("<h1>UMOWA WYNAJMU POJAZDU</h1>");
        sb.append("<p>zawarta w <strong>Dęblinie</strong> dnia <span class=\"underline\">");
        sb.append(rental.getRentalDate() != null ? rental.getRentalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
        sb.append("</span></p>");

        sb.append("<div class=\"section\">");
        sb.append("<p><strong>zawarta pomiędzy:</strong></p>");
        sb.append("<p><strong>LIDER S.C.</strong> Tomasz Kosiński, Wojciech Piątek, ul. Krzywa 4a, 08-530 Dęblin – zwanym dalej <strong>Wynajmującym</strong></p>");
        sb.append("<p>a</p>");
        sb.append("<p><span class=\"underline\">");
        sb.append(c.getFirstName() != null ? c.getFirstName() : "");
        sb.append(" ");
        sb.append(c.getLastName() != null ? c.getLastName() : "");
        sb.append("</span>, <span class=\"underline\">");
        sb.append(c.getAddress() != null ? c.getAddress() : "");
        sb.append("</span>,<br/>");
        sb.append("nr telefonu: <span class=\"underline\">");
        sb.append(c.getPhone() != null ? c.getPhone() : "");
        sb.append("</span>, PESEL: <span class=\"underline\">");
        sb.append(c.getPesel() != null ? c.getPesel() : "");
        sb.append("</span>,<br/>");
        sb.append("nr dowodu osobistego: <span class=\"underline\">");
        sb.append(c.getIdCardNumber() != null ? c.getIdCardNumber() : "");
        sb.append("</span>, nr prawa jazdy: <span class=\"underline\">");
        sb.append(c.getDrivingLicenseNumber() != null ? c.getDrivingLicenseNumber() : "");
        sb.append("</span>");

        if (c.getNip() != null && !c.getNip().isBlank()) {
            sb.append(", NIP: <span class=\"underline\">");
            sb.append(c.getNip());
            sb.append("</span>");
        }

        sb.append(" – zwanym dalej <strong>Najemcą</strong>.</p>");
        sb.append("</div>");

        sb.append("<div class=\"section\">");
        sb.append("<h2>§ 1 PRZEDMIOT UMOWY</h2>");
        sb.append("<p>Przedmiotem umowy jest pojazd o następujących danych:</p>");
        sb.append("<p><strong>Marka:</strong> <span class=\"underline\">");
        sb.append(car.getBrand() != null ? car.getBrand() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Model:</strong> <span class=\"underline\">");
        sb.append(car.getModel() != null ? car.getModel() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Nr rejestracyjny:</strong> <span class=\"underline\">");
        sb.append(car.getPlateNumber() != null ? car.getPlateNumber() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Data produkcji:</strong> <span class=\"underline\">");
        sb.append(car.getYear() != null ? String.valueOf(car.getYear()) : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Stan licznika przy odbiorze:</strong> <span class=\"underline\">");
        sb.append(rental.getStartMileage() != null ? String.valueOf(rental.getStartMileage()) : "");
        sb.append(" km</span></p>");
        sb.append("<p>Wypożyczany samochód to pojazd używany, sprawny technicznie, bez widocznych uszkodzeń i w takim też stanie ma zostać zwrócony przez Najemcę po zakończeniu najmu do miejsca odbioru. Posiada ważne ubezpieczenie OC.</p>");
        sb.append("</div>");

        sb.append("<div class=\"section\">");
        sb.append("<h2>§ 2 OKRES NAJMU I CZYNSZ</h2>");
        sb.append("<p><strong>Data i godzina wypożyczenia:</strong> <span class=\"underline\">");
        sb.append(rental.getStartDate() != null ? rental.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
        sb.append(" godz. ");
        sb.append(rental.getStartTime() != null ? rental.getStartTime() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Data i godzina zwrotu:</strong> <span class=\"underline\">");
        sb.append(rental.getEndDate() != null ? rental.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");  // ✅ getEndDate() zamiast getReturnDate()
        sb.append(" godz. ");
        sb.append(rental.getEndTime() != null ? rental.getEndTime() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Okres wynajmu:</strong> <span class=\"underline\">");
        sb.append(days);
        sb.append(" dni</span></p>");
        sb.append("<p><strong>Cena za dobę:</strong> <span class=\"underline\">");
        sb.append(rental.getPricePerDay() != null ? rental.getPricePerDay().toString() : "0");
        sb.append(" zł</span></p>");
        sb.append("<p><strong>Cena najmu:</strong> <span class=\"underline\">");
        sb.append(rental.getTotalPrice() != null ? rental.getTotalPrice().toString() : "0");
        sb.append(" zł</span></p>");
        sb.append("<p><strong>Kaucja:</strong> <span class=\"underline\">");
        sb.append(rental.getDeposit() != null ? rental.getDeposit().toString() : "0");
        sb.append(" zł</span></p>");
        sb.append("</div>");

        sb.append("<div class=\"section\">");
        sb.append("<h2>§ 3 OŚWIADCZENIE NAJEMCY</h2>");
        sb.append("<p>1. Potwierdzam otrzymanie Umowy i zobowiązuję się do stosowania warunków wynajmu, które stanowią integralną część umowy.</p>");
        sb.append("<p>2. Oświadczam, że wszystkie informacje i szczegółowe dane przekazane i zawarte w umowie najmu są prawdziwe.</p>");
        sb.append("<p>3. Potwierdzam odpowiedzialność za ewentualne koszty mycia, tankowania oraz wszelkich kar za naruszenie przepisów Kodeksu Ruchu Drogowego w czasie eksploatacji wynajętego pojazdu.</p>");
        sb.append("<p>4. Wyrażam zgodę na wystawienie faktury VAT bez wymogu złożenia podpisu odbiorcy na koniec miesiąca rozliczeniowego lub po zakończeniu umowy.</p>");
        sb.append("<p>5. Wyrażam zgodę na przetwarzanie danych osobowych celem wykonania niniejszej umowy.</p>");
        sb.append("</div>");

        // Podpisy po oświadczeniu najemcy:
        sb.append("<table style=\"width: 100%; margin-top: 40px; border-collapse: collapse;\">");
        sb.append("<tr>");
        sb.append("<td style=\"width: 50%; text-align: left; vertical-align: top;\">");
        sb.append("<strong>Wynajmujący</strong><br/>");
        sb.append("<div style=\"border-top: 1px solid #000; width: 200px; margin-top: 5px; padding-top: 40px;\"></div>");
        sb.append("</td>");
        sb.append("<td style=\"width: 50%; text-align: right; vertical-align: top;\">");
        sb.append("<strong>Najemca</strong><br/>");
        sb.append("<div style=\"border-top: 1px solid #000; width: 200px; margin-top: 5px; padding-top: 40px;\"></div>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");

        sb.append("<div class=\"page-break\"></div>");
        sb.append("<div class=\"section\">");
        sb.append("<h2>WARUNKI UMOWY</h2>");

        sb.append("<h3>I. Uprawnienia do kierowania pojazdem</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Najemcą samochodu i osobą, która jest uprawniona do kierowania pojazdem (dalej kierowca) może zostać osoba, która ukończyła 21 lat, posiada ważny dowód osobisty oraz od co najmniej dwóch lat posiada ważne na terytorium RP prawo jazdy (do okazania wymagane są dwa ważne dokumenty ze zdjęciem). Wskazane w niniejszym punkcie wymogi obowiązują przez cały okres trwania umowy najmu. W przypadku niespełnienia przez Najemcę wymogów wskazanych w niniejszym punkcie Wynajmujący będzie uprawniony do wypowiedzenia umowy najmu w trybie natychmiastowym.</p>");
        sb.append("<p><strong>2.</strong> Pojazdem może kierować osoba, która w umowie jest określona jako Najemca.</p>");
        sb.append("<p><strong>3.</strong> Wynajęty pojazd nie może być podnajęty lub oddany osobie trzeciej do używania bez uprzedniej pisemnej zgody Wynajmującego. Pojazd nie może także zostać oddany przez Najemcę do używania osobie nie wymienionej w umowie najmu jako kierowca, chyba że Wynajmujący wyrazi uprzednio pisemną zgodę. Przed udzieleniem zgody Najemca jest zobowiązany dostarczyć dokumenty potwierdzające spełnienie przez osobę trzecią wymaganych Regulaminem warunków. Przepisy regulaminu odnoszące się do Kierowcy mają zastosowanie również do Osoby upoważnionej.</p>");

        sb.append("<p><strong>§ 2.</strong></p>");
        sb.append("<p><strong>1.</strong> Wynajmujący nie ponosi odpowiedzialności za jakiekolwiek szkody powstałe w związku z korzystaniem przez Najemcę lub Kierowcę z przedmiotu najmu w trakcie jego trwania.</p>");
        sb.append("<p><strong>2.</strong> Wynajmujący nie ponosi również odpowiedzialności za rzeczy zagubione, przewożone, pozostawione w przedmiocie najmu, jak również za opłaty i należności wywołane zawinionym zachowaniem Najemcy lub Kierowcy w szczególności za mandaty, opłaty parkingowe czy opłaty za przejazd drogami płatnymi.</p>");

        sb.append("<p><strong>§ 3.</strong> W przypadku udostępnienia przez Najemcę przedmiotu najmu osobie nie spełniającej wymogów przewidzianych niniejszym Regulaminem, nieokreślonej umową najmu jako kierowca lub osobie nieupoważnionej przez Wynajmującego, zatrzymuje on wpłaconą przez Najemcę kaucję.</p>");

        sb.append("<p><strong>§ 4.</strong> Przedmiot najmu może być używany tylko na terenie Rzeczpospolitej Polskiej. W przypadku wyjazdu pojazdem poza granice Rzeczpospolitej Polskiej Najemca zobowiązany jest poinformować Wynajmującego i uzyskać od niego pisemną zgodę.</p>");

        sb.append("<h3>II. Obowiązki Najemcy</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Najemca zobowiązuje się do używania auta zgodnie z jego przeznaczeniem, w warunkach przewidzianych dla jego normalnej eksploatacji.</p>");
        sb.append("<p><strong>2.</strong> W przedmiocie najmu obowiązuje całkowity zakaz palenia.</p>");

        sb.append("<p><strong>§ 2.</strong></p>");
        sb.append("<p><strong>1.</strong> Przedmiot najmu w szczególności nie może być używany:</p>");
        sb.append("<p style=\"margin-left: 20px;\">a) przez osobę inną niż Najemca/Kierowca,</p>");
        sb.append("<p style=\"margin-left: 20px;\">b) do uruchamiania lub holowania innych pojazdów,</p>");
        sb.append("<p style=\"margin-left: 20px;\">c) do przewozu rzeczy, materiałów, substancji, które mogą spowodować znaczne zabrudzenia lub zniszczenia wnętrza pojazdu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">d) do przewozu zwierząt, dopuszczalne jest jedynie przewożenie małych zwierząt domowych po uprzednim zabezpieczeniu auta przed zanieczyszczeniem oraz zniszczeniem,</p>");
        sb.append("<p style=\"margin-left: 20px;\">e) do przewozu rzeczy lub osób w zakresie podnajmu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">f) do przewozu liczby osób lub masy ładunku przekraczających normy określone w dokumencie rejestracyjnym przedmiotu najmu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">g) niezgodnie z przeznaczeniem, w tym również w wyścigach, rajdach lub zawodach,</p>");
        sb.append("<p style=\"margin-left: 20px;\">h) w sposób niezgodny z obowiązującymi przepisami prawa, w szczególności prawa celnego oraz drogowego,</p>");
        sb.append("<p style=\"margin-left: 20px;\">i) w sytuacji gdy kierowca pojazdu pozostaje pod wpływem alkoholu, narkotyków lub innych substancji osłabiających jego świadomość i zdolność reakcji.</p>");

        sb.append("<p><strong>2.</strong> W przypadku naruszenia któregokolwiek z powyższych warunków Wynajmujący zatrzyma umówioną kaucję określoną przedmiotem najmu.</p>");
        sb.append("<p><strong>3.</strong> W przypadku naruszenia któregokolwiek z powyższych warunków w sytuacji gdy przedmiot najmu ulegnie uszkodzeniu Najemca zostanie obciążony kosztami naprawy, odbioru samochodu jak również kosztami za postój w wysokości czynszu najmu za każdą dobę.</p>");

        sb.append("<p><strong>§ 3.</strong> Najemca zobowiązany jest do:</p>");
        sb.append("<p style=\"margin-left: 20px;\">a) posiadania w trakcie korzystania z przedmiotu najmu ważnego na terytorium RP prawa jazdy,</p>");
        sb.append("<p style=\"margin-left: 20px;\">b) korzystania z pojazdu zgodnie z jego przeznaczeniem zachowując należytą staranność w zakresie eksploatacji pojazdu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">c) dokonania zabezpieczenia przedmiotu najmu przed kradzieżą m.in. poprzez każdorazowe jego zamykanie i włączenie alarmów, stosowanie innych dostępnych metod blokowania,</p>");
        sb.append("<p style=\"margin-left: 20px;\">d) stosowania w przedmiocie najmu odpowiedniego rodzaju paliwa zgodnie ze specyfikacją silnika, podaną w dowodzie rejestracyjnym oraz w dokumentacji technicznej pojazdu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">e) pokrywania kosztów bieżącej eksploatacji pojazdu zgodnie z podstawowymi normami m.in. uzupełnienie płynów do spryskiwacza, kontrola sprawności świateł i wymiana żarówek,</p>");
        sb.append("<p style=\"margin-left: 20px;\">f) pokrycia wszelkich opłat związanych z poruszaniem się po autostradach, drogach szybkiego ruchu, opłaty e-TOLL – w przypadku doczepienia przyczepy do pojazdu, mandatów oraz kar finansowych nałożonych na samochód w okresie, w którym był on użytkowany przez Najemcę.</p>");

        sb.append("<p><strong>§ 4.</strong></p>");
        sb.append("<p><strong>1.</strong> Najemca nie jest uprawniony do dokonywania w przedmiocie najmu zmian lub przeróbek sprzecznych z jego właściwościami i przeznaczeniem bez uprzedniej zgody Wynajmującego wyrażanej na piśmie.</p>");
        sb.append("<p><strong>2.</strong> W przypadku dokonania w przedmiocie najmu wyżej wymienionych zmian Wynajmujący jest uprawniony do obciążenia Najemcy kosztami przywrócenia stanu poprzedniego oraz żądania wyrównania szkody związanej z obciążeniem wartości pojazdu spowodowanego przedmiotowym działaniem Najemcy.</p>");

        sb.append("<p><strong>§ 5.</strong></p>");
        sb.append("<p><strong>1.</strong> Wynajmujący lub osoba przez niego upoważniona, mają prawo do kontrolowania najemcy w zakresie sposobu wykorzystania przedmiotu najmu oraz jego stanu w przypadku powzięcia informacji o naruszaniu regulaminu przez Najemcę.</p>");
        sb.append("<p><strong>2.</strong> Najemca ma obowiązek umożliwienia kontroli oraz udostępnienia dokumentów, Wynajmującemu bądź osobie przez niego uprawnionej.</p>");

        sb.append("<h3>III. Zwrot pojazdu, przedmiotu najmu</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Po zakończeniu umowy najmu, Najemca zobowiązany jest do zwrotu przedmiotu najmu w miejscu i czasie ustalonym w umowie, to jest w Dęblinie przy ulicy Krzywej 4a. Samochód powinien być umyty z zewnątrz oraz wyczyszczony wewnątrz. W przypadku zwrotu brudnego pojazdu do ceny najmu będzie doliczona jednorazowa opłata 150 zł za mycie pojazdu.</p>");
        sb.append("<p><strong>2.</strong> Zbiornik paliwa powinien być zatankowany do pełna. W przypadku zwrotu samochodu z niepełnym zbiornikiem paliwa Najemca zobowiązany jest do pokrycia kosztów brakującego paliwa.</p>");

        sb.append("<p><strong>§ 2.</strong></p>");
        sb.append("<p><strong>1.</strong> Możliwy jest zwrot przedmiotu najmu w innym miejscu po wcześniejszym uzgodnieniu z Wynajmującym, za dodatkową opłatą.</p>");
        sb.append("<p><strong>2.</strong> W przypadku zwrotu przedmiotu najmu w innym miejscu niż określono w umowie, bez wcześniejszego uzgodnienia z Wynajmującym Najemca zobowiązany będzie do pokrycia powstałych w związku z tym kosztów.</p>");
        sb.append("<p><strong>3.</strong> W sytuacji pozostawienia przedmiotu najmu uszkodzonego lub niesprawnego Najemca pokrywa również koszty holowania. Przedmiotowe koszty mogą być pokryte przez Wynajmującego z wniesionej kaucji.</p>");

        sb.append("<p><strong>§ 3.</strong></p>");
        sb.append("<p><strong>1.</strong> Bieg okresu najmu rozpoczyna się od określonej w umowie najmu daty i godziny.</p>");
        sb.append("<p><strong>2.</strong> Doba najmu rozumiana jest jako kolejne 24 godziny poczynając od określonej w umowie daty i godziny.</p>");

        sb.append("<p><strong>§ 4.</strong> W przypadku użytkowania przedmiotowego samochodu po dniu wygaśnięcia umowy Wynajmujący obciąży Najemcę karą umowną w wysokości 500 PLN za każdą rozpoczętą dobę wystawiając na rzecz Najemcy notę księgową. O bezumownym korzystaniu z samochodu zostaną powiadomione odpowiednie organy ścigania, w tym Policja.</p>");

        sb.append("<p><strong>§ 5.</strong> Najemca może wnioskować o przedłużenie umowy najmu pisemnie w formie maila (tomasz.kosinski@wp.pl) lub SMS na nr 506137418 nie później niż dzień przed datą wygaśnięcia umowy. Umowa zostaje przedłużona, jeżeli Wynajmujący wyrazi zgodę.</p>");

        sb.append("<h3>IV. Postępowanie w przypadku awarii i kradzieży</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> W przypadku wystąpienia jakiejkolwiek awarii technicznej przedmiotu najmu, Najemca/Kierowca zobowiązany jest do zabezpieczenia pojazdu oraz do niezwłocznego poinformowania Wynajmującego o zdarzeniu udzielając wszelkich informacji dotyczących jego stanu oraz miejsca jego postoju. Wynajmujący wskaże najbliższy autoryzowany serwis. Najemca ponosi koszty naprawy w serwisie wskazanym przez wynajmującego. Rozliczenie kosztów naprawy nastąpi w momencie zwrotu pojazdu Wynajmującemu na podstawie faktury wystawionej przez serwis na Wynajmującego. Czas najmu pojazdu zostaje przedłużony o czas pozostawania pojazdu w serwisie lub o czas dostarczenia pojazdu zastępczego. Wynajmujący nie ponosi odpowiedzialności za szkody poniesione przez najemcę w skutek awarii samochodu. Jeżeli awaria techniczna przedmiotu najmu wystąpiła bez winy Najemcy/Kierowcy, Wynajmujący może w miarę możliwości wynająć najemcy inny pojazd.</p>");
        sb.append("<p><strong>2.</strong> Najemca nie może dokonywać napraw przedmiotu najmu bez zgody i wiedzy Wynajmującego. Nie może również podejmować działań mogących stanowić zagrożenie dla bezpieczeństwa ruchu drogowego.</p>");

        sb.append("<p><strong>§ 2.</strong></p>");
        sb.append("<p><strong>1.</strong> W przypadku kradzieży przedmiotu najmu, zaistnienia zdarzenia drogowego z udziałem przedmiotu najmu Najemca/Kierowca zobowiązany jest wezwać Policję na miejsce zdarzenia, zażądać od Policji wydania kopii notatki, protokołu lub innego dokumentu potwierdzającego fakt zaistnienia któregokolwiek z przedmiotów zdarzeń.</p>");
        sb.append("<p><strong>2.</strong> Najemca zobowiązany jest również do poinformowania o opisanych powyżej zdarzeniach oraz niezwłocznego przekazania uzyskanych dokumentów oraz informacji, które będą Wynajmującemu do dochodzenia roszczeń powstałych w związku z opisanymi powyżej zdarzeniami.</p>");
        sb.append("<p><strong>3.</strong> Najemca zobowiązany jest do udzielenia powyższych informacji Wynajmującemu.</p>");

        sb.append("<p><strong>§ 3.</strong></p>");
        sb.append("<p><strong>1.</strong> W przypadku zawinionego przez Najemcę uszkodzenia samochodu kaucja Najemcy zostanie wstrzymana do czasu ustalenia kosztów czynności związanych z likwidacją naprawy. Po ustaleniu wysokości kosztów strony dokonają rozliczenia.</p>");
        sb.append("<p><strong>2.</strong> W przypadku gdy koszty przekraczają wysokość kaucji Najemca będzie zobowiązany do pokrycia powstałej nadwyżki, natomiast gdy koszty będą niższe od wysokości kaucji różnica zostanie zwrócona Najemcy.</p>");

        sb.append("<p><strong>§ 4.</strong> Zagubienie dokumentów lub kluczyków traktowane będzie jak złamanie niniejszej umowy najmu. Kaucja nie zostanie zwrócona, najemca poniesie pełny koszt odtworzenia dokumentów i (lub) kluczyków do pojazdu.</p>");

        sb.append("<h3>VII. Odpowiedzialność i opłaty</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Za wynajem pojazdu Najemca uiszcza opłatę z góry bądź na podstawie faktury VAT wystawionej przez Wynajmującego w dniu zawarcia umowy na rachunek bankowy Wynajmującego wskazany na fakturze.</p>");
        sb.append("<p><strong>2.</strong> Za datę zapłaty Strony uznają datę obciążenia rachunku bankowego Najemcy.</p>");

        sb.append("<p><strong>§ 2.</strong> W przewidzianych Umową wypadkach obciążenia karą umowną, Wynajmujący ma prawo żądać od Najemcy zapłaty odszkodowania uzupełniającego, jeżeli szkoda poniesiona przez Wynajmującego przewyższa wartość kary umownej.</p>");

        sb.append("<p><strong>§ 3.</strong></p>");
        sb.append("<p><strong>1.</strong> Dzienny limit kilometrów wynosi 500 kilometrów przy wynajmie na 1 dobę.</p>");
        sb.append("<p><strong>2.</strong> W przypadku przekroczenia określonego limitu Najemca zobowiązuje się do zapłaty kwoty 0,50 zł brutto za każdy kilometr ponad wskazany limit.</p>");
        sb.append("<p><strong>3.</strong> W celu zaspokojenia niniejszego roszczenia Wynajmujący zatrzymuje kaucję złożoną przez Najemcę, o ile nie została ona rozliczona na poczet innego roszczenia Wynajmującego.</p>");

        sb.append("<p><strong>§ 4.</strong> Wynajmujący nie jest odpowiedzialny wobec osób trzecich za jakiekolwiek roszczenia odszkodowawcze będące następstwem szkody spowodowanej przez Najemcę lub Kierowcę w okresie najmu.</p>");

        sb.append("<p><strong>§ 5.</strong> Jeżeli Najemca będzie zalegał w opłacaniu czynszu dłużej niż 7 dni, Wynajmujący wezwie Najemcę do zapłaty długu wyznaczając w tym zakresie termin nie dłuższy niż 3 dni. Po upływie tego terminu i braku uregulowania należności będzie to równoznaczne z rozwiązaniem umowy. Wynajmujący uprawniony będzie do odebrania pojazdu, co nie będzie stanowić zaboru wynajętego mienia. Kosztami odbioru zostanie obciążony Najemca.</p>");

        sb.append("<p><strong>§ 6.</strong> Samochód posiada ubezpieczenie na warunkach firmy ubezpieczeniowej w zakresie OC, AC, NW oraz Assistance. Najemca zobowiązany jest do zapoznania się oraz przestrzegania warunków umowy ubezpieczenia. W przypadku utraty pojazdu wraz z dowodem rejestracyjnym i (lub) kluczykami najemca ponosi pełną odpowiedzialność materialną. Złamanie warunków ubezpieczenia traktowane będzie jak złamanie warunków umowy najmu i skutkuje pełną odpowiedzialnością materialną wynajmującego. Najemca przyjmuje na siebie pełną odpowiedzialność materialną za szkody powstałe z własnej winy a nie objęte polisą AC. Za szkody objęte polisą AC użytkownik odpowiada do kwoty udziału własnego tj. 2000 zł. W przypadku szkód do 2000 zł, użytkownik ponosi pełny koszt naprawy pojazdu. Najemca ponosi pełny koszt naprawy uszkodzeń wnętrza (tapicerka, pasy bezpieczeństwa, radio, nawigacja, itp.) wynikających z winy najemcy a nie związanych z kolizją lub kradzieżą pojazdu. Wszelkie zaplamienia tapicerki powstałe w czasie najmu zostaną usunięte na koszt wynajmującego.</p>");

        sb.append("<h3>VIII. Postanowienia końcowe</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Najemca oświadcza, że zapoznał się z umową najmu.</p>");
        sb.append("<p><strong>2.</strong> Najemca oświadcza również, iż zapoznał się z postanowieniami umowy oraz cennika i je akceptuje.</p>");

        sb.append("<p><strong>§ 2.</strong> Najemca upoważnia Wynajmującego do wystawienia Faktury VAT bez podpisu Najemcy zgodnie z postanowieniami umowy.</p>");

        sb.append("<p><strong>§ 3.</strong> Najemca oświadcza, że wyraża zgodę na przetwarzanie swoich danych osobowych przez Wynajmującego.</p>");

        sb.append("<p><strong>§ 4.</strong> Najemca potwierdza, że podane do Umowy dane są prawdziwe, wyraża zgodę na ich umieszczenie w bazie danych Wynajmującego, który będzie ich administratorem, na ich przekazywanie osobom trzecim, a także na ich przetwarzanie zgodnie z ustawą z dnia 29.08.1997 r. o ochronie danych osobowych (Dz. U. Nr 133 poz. 883) w celu wykonania Umowy, zabezpieczenia transakcji oraz w celach marketingowych.</p>");

        sb.append("<p><strong>§ 5.</strong> Najemca oświadcza, że został poinformowany o prawie wglądu do swoich danych i możliwości żądania uzupełnienia, uaktualnienia, sprostowania oraz czasowego lub stałego wstrzymania ich przetwarzania lub ich usunięcia.</p>");

        sb.append("<p><strong>§ 6.</strong> W sprawach nieuregulowanych w niniejszej umowie będą miały zastosowanie odpowiednie przepisy Kodeksu Cywilnego.</p>");

        sb.append("<p><strong>§ 7.</strong> Sądem właściwym do rozpoznania ewentualnych sporów wynikających z niniejszej umowy jest Sąd właściwy ze względu na miejsce siedziby Wynajmującego.</p>");

        sb.append("<p><strong>§ 8.</strong> Wszelkie zmiany niniejszej umowy będą dokonywane przez strony wyłącznie w formie pisemnej pod rygorem nieważności.</p>");

        sb.append("<p><strong>§ 9.</strong> Umowę sporządzono w dwóch jednakowych egzemplarzach po jednym dla każdej strony.</p>");
        sb.append("</div>");
        sb.append("<div class=\"section\">");
        sb.append("<h2>Obowiązek informacyjny z art. 13 RODO</h2>");
        sb.append("<p>Zgodnie z art. 13 rozporządzenia Parlamentu Europejskiego i Rady (UE) 2016/679 z dnia 27 kwietnia 2016 r. w sprawie ochrony osób fizycznych w związku z przetwarzaniem danych osobowych i w sprawie swobodnego przepływu takich danych oraz uchylenia dyrektywy 95/46/WE (tekst w języku polskim: Dziennik Urzędowy Unii Europejskiej, Nr 4.5.2016) (RODO), informujemy iż:</p>");
        sb.append("<p>Administratorem Pana/Pani danych osobowych jest LIDER S.C. ul. Krzywa 4a 08-530 Dęblin.</p>");
        sb.append("<p>Podstawę przetwarzania Pana/Pani danych osobowych stanowi wyrażona przez Pana/Panią zgoda.</p>");
        sb.append("<p>Administrator danych nie ma zamiaru przekazywać Pana/Pani danych osobowych do państwa trzeciego lub organizacji międzynarodowej, w tym również do takich w stosunku do których Komisja Europejska stwierdziła odpowiedni stopień ochrony.</p>");
        sb.append("<p>Okres, przez który dane osobowe będą przechowywane: do czasu odwołania zgody.</p>");
        sb.append("<p>Przysługuje Panu/Pani prawo do żądania od administratora danych dostępu do danych osobowych Pana/Pani dotyczących, ich sprostowania, usunięcia lub ograniczenia, a także o prawo do przenoszenia danych.</p>");
        sb.append("<p>Przysługuje Pani/Panu prawo do cofnięcia zgody w dowolnym momencie bez wpływu na zgodność z prawem przetwarzania, którego dokonano na podstawie zgody przed jej cofnięciem.</p>");
        sb.append("<p>Przysługuje Pani/Panu prawo do wniesienia skargi do polskiego organu nadzorczego lub organu nadzorczego innego państwa członkowskiego Unii Europejskiej, właściwego ze względu na miejsce zwykłego pobytu lub pracy lub ze względu na miejsce domniemanego naruszenia RODO.</p>");
        sb.append("<p>Podanie przez Panią/Pana danych osobowych jest dobrowolne, nie jest wymogiem ustawowym, umownym, lub warunkiem zawarcia umowy. Nie jest Pan/Pani zobowiązany do podania danych osobowych. Nie ma żadnych konsekwencji niepodania danych osobowych poza tym, iż w takim przypadku nie będą do Pana/Pani kierowane informacje marketingowe.</p>");
        sb.append("<p>W ramach prowadzenia działań marketingowych nie są podejmowane zautomatyzowane decyzje. Dane osobowe nie są profilowane.</p>");
        sb.append("<p>Zgoda może zostać cofnięta w dowolnym momencie, a wycofanie zgody nie wpływa na zgodność z prawem przetwarzania, którego dokonano na podstawie zgody przed jej wycofaniem.</p>");
        sb.append("</div>");

        sb.append("<table style=\"width: 100%; margin-top: 60px; border-collapse: collapse;\">");
        sb.append("<tr>");
        sb.append("<td style=\"width: 50%; text-align: left; vertical-align: top;\">");
        sb.append("<strong>Wynajmujący</strong><br/>");
        sb.append("<div style=\"border-top: 1px solid #000; width: 200px; margin-top: 5px; padding-top: 40px;\"></div>");
        sb.append("<p style=\"margin-top: 5px;\">LIDER S.C.</p>");
        sb.append("</td>");
        sb.append("<td style=\"width: 50%; text-align: right; vertical-align: top;\">");
        sb.append("<strong>Najemca</strong><br/>");
        sb.append("<div style=\"border-top: 1px solid #000; width: 200px; margin-top: 5px; padding-top: 40px;\"></div>");
        sb.append("<p style=\"margin-top: 5px;\">");
        sb.append(c.getFirstName() != null ? c.getFirstName() : "");
        sb.append(" ");
        sb.append(c.getLastName() != null ? c.getLastName() : "");
        sb.append("</p>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");

        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }

    // ==================== NOWA METODA DLA LONG-TERM ====================
    public byte[] generateLongTermContract(Rental rental) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(
                    () -> getClass().getResourceAsStream("/fonts/DejaVuSans.ttf"),
                    "DejaVu Sans");
            builder.withHtmlContent(buildLongTermHtml(rental), "");
            builder.toStream(out);
            builder.run();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Błąd generowania PDF dla umowy long-term", e);
        }
    }

    // ==================== BUDOWANIE HTML DLA UMOWY LONG-TERM ====================
    private String buildLongTermHtml(Rental rental) {
        Customer c = rental.getCustomer();
        Car car = rental.getCar();

        java.time.format.DateTimeFormatter dateFormatter =
                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
        java.time.format.DateTimeFormatter timeFormatter =
                java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        String contractDate = rental.getRentalDate() != null
                ? rental.getRentalDate().toLocalDate().format(dateFormatter)
                : java.time.LocalDate.now().format(dateFormatter);

        String startTime = rental.getStartTime() != null
                ? rental.getStartTime().format(timeFormatter) : "00:00";
        String endTime = rental.getEndTime() != null
                ? rental.getEndTime().format(timeFormatter) : "00:00";

        String daysCount = "";
        if (rental.getStartDate() != null && rental.getEndDate() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    rental.getStartDate(),
                    rental.getEndDate()
            );
            daysCount = String.valueOf(days);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("""    
<!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8"/>
                <style>
                body {
            font-family: 'DejaVu Sans';
            font-size: 11px;
            line-height: 1.4;
            padding: 20px;
        }
        h1 {
            text-align: center;
            font-size: 16px;
            margin-bottom: 20px;
            text-decoration: underline;
        }
        h2 {
            font-size: 13px;
            margin-top: 18px;
            margin-bottom: 8px;
        }
        h3 {
                                font-size: 12px;
                                margin-top: 15px;
                                margin-bottom: 6px;
                                font-weight: bold;
                            }
        p {
            margin: 4px 0;
        }
    .section {
            margin-top: 15px;
        }
    .underline {
            border-bottom: 1px solid #000;
            display: inline-block;
            min-width: 200px;
            padding: 2px 0;
        }
    .signature-block {
            margin-top: 60px;
            display: flex;
            justify-content: space-between;
            page-break-inside: avoid;
        }
    .signature {
            width: 45%;
            text-align: center;
        }
    .regulations {
            font-size: 10px;
            margin-top: 30px;
            page-break-before: always;
        }
        .page-break {
                                page-break-before: always;
                                break-before: page;
                            }
</style>
                </head>
                <body>
                """);

        sb.append("<h1>UMOWA WYNAJMU POJAZDU</h1>");
        sb.append("<p>zawarta w <strong>Dęblinie</strong> dnia <span class=\"underline\">");
        sb.append(rental.getRentalDate() != null ? rental.getRentalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
        sb.append("</span></p>");

        sb.append("<div class=\"section\">");
        sb.append("<p><strong>zawarta pomiędzy:</strong></p>");
        sb.append("<p><strong>LIDER S.C.</strong> Tomasz Kosiński, Wojciech Piątek, ul. Krzywa 4a, 08-530 Dęblin – zwanym dalej <strong>Wynajmującym</strong></p>");
        sb.append("<p>a</p>");
        sb.append("<p><span class=\"underline\">");
        sb.append(c.getFirstName() != null ? c.getFirstName() : "");
        sb.append(" ");
        sb.append(c.getLastName() != null ? c.getLastName() : "");
        sb.append("</span>, <span class=\"underline\">");
        sb.append(c.getAddress() != null ? c.getAddress() : "");
        sb.append("</span>,<br/>");
        sb.append("nr telefonu: <span class=\"underline\">");
        sb.append(c.getPhone() != null ? c.getPhone() : "");
        sb.append("</span>, PESEL: <span class=\"underline\">");
        sb.append(c.getPesel() != null ? c.getPesel() : "");
        sb.append("</span>,<br/>");
        sb.append("nr dowodu osobistego: <span class=\"underline\">");
        sb.append(c.getIdCardNumber() != null ? c.getIdCardNumber() : "");
        sb.append("</span>, nr prawa jazdy: <span class=\"underline\">");
        sb.append(c.getDrivingLicenseNumber() != null ? c.getDrivingLicenseNumber() : "");
        sb.append("</span>");

        if (c.getNip() != null && !c.getNip().isBlank()) {
            sb.append(", NIP: <span class=\"underline\">");
            sb.append(c.getNip());
            sb.append("</span>");
        }

        sb.append(" – zwanym dalej <strong>Najemcą</strong>.</p>");
        sb.append("</div>");

        sb.append("<div class=\"section\">");
        sb.append("<h2>§ 1 PRZEDMIOT UMOWY</h2>");
        sb.append("<p>Przedmiotem umowy jest pojazd o następujących danych:</p>");
        sb.append("<p><strong>Marka:</strong> <span class=\"underline\">");
        sb.append(car.getBrand() != null ? car.getBrand() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Model:</strong> <span class=\"underline\">");
        sb.append(car.getModel() != null ? car.getModel() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Nr rejestracyjny:</strong> <span class=\"underline\">");
        sb.append(car.getPlateNumber() != null ? car.getPlateNumber() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Data produkcji:</strong> <span class=\"underline\">");
        sb.append(car.getYear() != null ? String.valueOf(car.getYear()) : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Stan licznika przy odbiorze:</strong> <span class=\"underline\">");
        sb.append(rental.getStartMileage() != null ? String.valueOf(rental.getStartMileage()) : "");
        sb.append(" km</span></p>");
        sb.append("<p>Wypożyczany samochód to pojazd używany, sprawny technicznie, bez widocznych uszkodzeń i w takim też stanie ma zostać zwrócony przez Najemcę po zakończeniu najmu do miejsca odbioru. Posiada ważne ubezpieczenie OC.</p>");
        sb.append("</div>");

        // ✅ LICZYMY MIESIĄCE zamiast dni
        long months = ChronoUnit.MONTHS.between(
                rental.getStartDate().withDayOfMonth(1),
                rental.getEndDate().withDayOfMonth(1)
        );
        if (months == 0) months = 1;

        sb.append("<div class=\"section\">");
        sb.append("<h2>§ 2 OKRES NAJMU I CZYNSZ</h2>");
        sb.append("<p><strong>Data i godzina wypożyczenia:</strong> <span class=\"underline\">");
        sb.append(rental.getStartDate() != null ? rental.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
        sb.append(" godz. ");
        sb.append(rental.getStartTime() != null ? rental.getStartTime() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Data i godzina zwrotu:</strong> <span class=\"underline\">");
        sb.append(rental.getEndDate() != null ? rental.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
        sb.append(" godz. ");
        sb.append(rental.getEndTime() != null ? rental.getEndTime() : "");
        sb.append("</span></p>");
        sb.append("<p><strong>Okres wynajmu:</strong> <span class=\"underline\">");
        sb.append(months);
        sb.append(" miesięcy</span></p>");
        sb.append("<p><strong>Cena za miesiąc:</strong> <span class=\"underline\">");
        sb.append(rental.getPricePerMonth() != null ? rental.getPricePerMonth().toString() : "0");
        sb.append(" zł</span></p>");
        sb.append("<p><strong>Cena najmu:</strong> <span class=\"underline\">");
        sb.append(rental.getTotalPrice() != null ? rental.getTotalPrice().toString() : "0");
        sb.append(" zł</span></p>");
        sb.append("<p><strong>Kaucja:</strong> <span class=\"underline\">");
        sb.append(rental.getDeposit() != null ? rental.getDeposit().toString() : "0");
        sb.append(" zł</span></p>");
        sb.append("</div>");

        sb.append("<div class=\"section\">");
        sb.append("<h2>§ 3 OŚWIADCZENIE NAJEMCY</h2>");
        sb.append("<p>1. Potwierdzam otrzymanie Umowy i zobowiązuję się do stosowania warunków wynajmu, które stanowią integralną część umowy.</p>");
        sb.append("<p>2. Oświadczam, że wszystkie informacje i szczegółowe dane przekazane i zawarte w umowie najmu są prawdziwe.</p>");
        sb.append("<p>3. Potwierdzam odpowiedzialność za ewentualne koszty mycia, tankowania oraz wszelkich kar za naruszenie przepisów Kodeksu Ruchu Drogowego w czasie eksploatacji wynajętego pojazdu.</p>");
        sb.append("<p>4. Wyrażam zgodę na wystawienie faktury VAT bez wymogu złożenia podpisu odbiorcy na koniec miesiąca rozliczeniowego lub po zakończeniu umowy.</p>");
        sb.append("<p>5. Wyrażam zgodę na przetwarzanie danych osobowych celem wykonania niniejszej umowy.</p>");
        sb.append("</div>");

        // Podpisy po oświadczeniu najemcy:
        sb.append("<table style=\"width: 100%; margin-top: 40px; border-collapse: collapse;\">");
        sb.append("<tr>");
        sb.append("<td style=\"width: 50%; text-align: left; vertical-align: top;\">");
        sb.append("<strong>Wynajmujący</strong><br/>");
        sb.append("<div style=\"border-top: 1px solid #000; width: 200px; margin-top: 5px; padding-top: 40px;\"></div>");
        sb.append("</td>");
        sb.append("<td style=\"width: 50%; text-align: right; vertical-align: top;\">");
        sb.append("<strong>Najemca</strong><br/>");
        sb.append("<div style=\"border-top: 1px solid #000; width: 200px; margin-top: 5px; padding-top: 40px;\"></div>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");

        sb.append("<div class=\"page-break\"></div>");
        sb.append("<div class=\"section\">");
        sb.append("<h2>WARUNKI UMOWY</h2>");

        sb.append("<h3>I. Uprawnienia do kierowania pojazdem</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Najemcą samochodu i osobą, która jest uprawniona do kierowania pojazdem (dalej kierowca) może zostać osoba, która ukończyła 21 lat, posiada ważny dowód osobisty oraz od co najmniej dwóch lat posiada ważne na terytorium RP prawo jazdy (do okazania wymagane są dwa ważne dokumenty ze zdjęciem). Wskazane w niniejszym punkcie wymogi obowiązują przez cały okres trwania umowy najmu. W przypadku niespełnienia przez Najemcę wymogów wskazanych w niniejszym punkcie Wynajmujący będzie uprawniony do wypowiedzenia umowy najmu w trybie natychmiastowym.</p>");
        sb.append("<p><strong>2.</strong> Pojazdem może kierować osoba, która w umowie jest określona jako Najemca.</p>");
        sb.append("<p><strong>3.</strong> Wynajęty pojazd nie może być podnajęty lub oddany osobie trzeciej do używania bez uprzedniej pisemnej zgody Wynajmującego. Pojazd nie może także zostać oddany przez Najemcę do używania osobie nie wymienionej w umowie najmu jako kierowca, chyba że Wynajmujący wyrazi uprzednio pisemną zgodę. Przed udzieleniem zgody Najemca jest zobowiązany dostarczyć dokumenty potwierdzające spełnienie przez osobę trzecią wymaganych Regulaminem warunków. Przepisy regulaminu odnoszące się do Kierowcy mają zastosowanie również do Osoby upoważnionej.</p>");

        sb.append("<p><strong>§ 2.</strong></p>");
        sb.append("<p><strong>1.</strong> Wynajmujący nie ponosi odpowiedzialności za jakiekolwiek szkody powstałe w związku z korzystaniem przez Najemcę lub Kierowcę z przedmiotu najmu w trakcie jego trwania.</p>");
        sb.append("<p><strong>2.</strong> Wynajmujący nie ponosi również odpowiedzialności za rzeczy zagubione, przewożone, pozostawione w przedmiocie najmu, jak również za opłaty i należności wywołane zawinionym zachowaniem Najemcy lub Kierowcy w szczególności za mandaty, opłaty parkingowe czy opłaty za przejazd drogami płatnymi.</p>");

        sb.append("<p><strong>§ 3.</strong> W przypadku udostępnienia przez Najemcę przedmiotu najmu osobie nie spełniającej wymogów przewidzianych niniejszym Regulaminem, nieokreślonej umową najmu jako kierowca lub osobie nieupoważnionej przez Wynajmującego, zatrzymuje on wpłaconą przez Najemcę kaucję.</p>");

        sb.append("<p><strong>§ 4.</strong> Przedmiot najmu może być używany tylko na terenie Rzeczpospolitej Polskiej. W przypadku wyjazdu pojazdem poza granice Rzeczpospolitej Polskiej Najemca zobowiązany jest poinformować Wynajmującego i uzyskać od niego pisemną zgodę.</p>");

        sb.append("<h3>II. Obowiązki Najemcy</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Najemca zobowiązuje się do używania auta zgodnie z jego przeznaczeniem, w warunkach przewidzianych dla jego normalnej eksploatacji.</p>");
        sb.append("<p><strong>2.</strong> W przedmiocie najmu obowiązuje całkowity zakaz palenia.</p>");

        sb.append("<p><strong>§ 2.</strong></p>");
        sb.append("<p><strong>1.</strong> Przedmiot najmu w szczególności nie może być używany:</p>");
        sb.append("<p style=\"margin-left: 20px;\">a) przez osobę inną niż Najemca/Kierowca,</p>");
        sb.append("<p style=\"margin-left: 20px;\">b) do uruchamiania lub holowania innych pojazdów,</p>");
        sb.append("<p style=\"margin-left: 20px;\">c) do przewozu rzeczy, materiałów, substancji, które mogą spowodować znaczne zabrudzenia lub zniszczenia wnętrza pojazdu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">d) do przewozu zwierząt, dopuszczalne jest jedynie przewożenie małych zwierząt domowych po uprzednim zabezpieczeniu auta przed zanieczyszczeniem oraz zniszczeniem,</p>");
        sb.append("<p style=\"margin-left: 20px;\">e) do przewozu rzeczy lub osób w zakresie podnajmu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">f) do przewozu liczby osób lub masy ładunku przekraczających normy określone w dokumencie rejestracyjnym przedmiotu najmu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">g) niezgodnie z przeznaczeniem, w tym również w wyścigach, rajdach lub zawodach,</p>");
        sb.append("<p style=\"margin-left: 20px;\">h) w sposób niezgodny z obowiązującymi przepisami prawa, w szczególności prawa celnego oraz drogowego,</p>");
        sb.append("<p style=\"margin-left: 20px;\">i) w sytuacji gdy kierowca pojazdu pozostaje pod wpływem alkoholu, narkotyków lub innych substancji osłabiających jego świadomość i zdolność reakcji.</p>");

        sb.append("<p><strong>2.</strong> W przypadku naruszenia któregokolwiek z powyższych warunków Wynajmujący zatrzyma umówioną kaucję określoną przedmiotem najmu.</p>");
        sb.append("<p><strong>3.</strong> W przypadku naruszenia któregokolwiek z powyższych warunków w sytuacji gdy przedmiot najmu ulegnie uszkodzeniu Najemca zostanie obciążony kosztami naprawy, odbioru samochodu jak również kosztami za postój w wysokości czynszu najmu za każdą dobę.</p>");

        sb.append("<p><strong>§ 3.</strong> Najemca zobowiązany jest do:</p>");
        sb.append("<p style=\"margin-left: 20px;\">a) posiadania w trakcie korzystania z przedmiotu najmu ważnego na terytorium RP prawa jazdy,</p>");
        sb.append("<p style=\"margin-left: 20px;\">b) korzystania z pojazdu zgodnie z jego przeznaczeniem zachowując należytą staranność w zakresie eksploatacji pojazdu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">c) dokonania zabezpieczenia przedmiotu najmu przed kradzieżą m.in. poprzez każdorazowe jego zamykanie i włączenie alarmów, stosowanie innych dostępnych metod blokowania,</p>");
        sb.append("<p style=\"margin-left: 20px;\">d) stosowania w przedmiocie najmu odpowiedniego rodzaju paliwa zgodnie ze specyfikacją silnika, podaną w dowodzie rejestracyjnym oraz w dokumentacji technicznej pojazdu,</p>");
        sb.append("<p style=\"margin-left: 20px;\">e) pokrywania kosztów bieżącej eksploatacji pojazdu zgodnie z podstawowymi normami m.in. uzupełnienie płynów do spryskiwacza, kontrola sprawności świateł i wymiana żarówek, kontroli poziomu oleju</p>");
        sb.append("<p style=\"margin-left: 20px;\">f) pokrycia wszelkich opłat związanych z poruszaniem się po autostradach, drogach szybkiego ruchu, opłaty e-TOLL – w przypadku doczepienia przyczepy do pojazdu, mandatów oraz kar finansowych nałożonych na samochód w okresie, w którym był on użytkowany przez Najemcę.</p>");

        sb.append("<p><strong>§ 4.</strong></p>");
        sb.append("<p><strong>1.</strong> Najemca nie jest uprawniony do dokonywania w przedmiocie najmu zmian lub przeróbek sprzecznych z jego właściwościami i przeznaczeniem bez uprzedniej zgody Wynajmującego wyrażanej na piśmie.</p>");
        sb.append("<p><strong>2.</strong> W przypadku dokonania w przedmiocie najmu wyżej wymienionych zmian Wynajmujący jest uprawniony do obciążenia Najemcy kosztami przywrócenia stanu poprzedniego oraz żądania wyrównania szkody związanej z obciążeniem wartości pojazdu spowodowanego przedmiotowym działaniem Najemcy.</p>");

        sb.append("<p><strong>§ 5.</strong></p>");
        sb.append("<p><strong>1.</strong> Wynajmujący lub osoba przez niego upoważniona, mają prawo do kontrolowania najemcy w zakresie sposobu wykorzystania przedmiotu najmu oraz jego stanu w przypadku powzięcia informacji o naruszaniu regulaminu przez Najemcę.</p>");
        sb.append("<p><strong>2.</strong> Najemca ma obowiązek umożliwienia kontroli oraz udostępnienia dokumentów, Wynajmującemu bądź osobie przez niego uprawnionej.</p>");

        sb.append("<h3>III. Zwrot pojazdu, przedmiotu najmu</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Po zakończeniu umowy najmu, Najemca zobowiązany jest do zwrotu przedmiotu najmu w miejscu i czasie ustalonym w umowie, to jest w Dęblinie przy ulicy Krzywej 4a. Samochód powinien być umyty z zewnątrz oraz wyczyszczony wewnątrz. W przypadku zwrotu brudnego pojazdu do ceny najmu będzie doliczona jednorazowa opłata 300 zł za mycie pojazdu.</p>");
        sb.append("<p><strong>2.</strong> Zbiornik paliwa powinien być zatankowany do pełna. W przypadku zwrotu samochodu z niepełnym zbiornikiem paliwa Najemca zobowiązany jest do pokrycia kosztów brakującego paliwa.</p>");

        sb.append("<p><strong>§ 2.</strong></p>");
        sb.append("<p><strong>1.</strong> Możliwy jest zwrot przedmiotu najmu w innym miejscu po wcześniejszym uzgodnieniu z Wynajmującym, za dodatkową opłatą.</p>");
        sb.append("<p><strong>2.</strong> W przypadku zwrotu przedmiotu najmu w innym miejscu niż określono w umowie, bez wcześniejszego uzgodnienia z Wynajmującym Najemca zobowiązany będzie do pokrycia powstałych w związku z tym kosztów.</p>");
        sb.append("<p><strong>3.</strong> W sytuacji pozostawienia przedmiotu najmu uszkodzonego lub niesprawnego Najemca pokrywa również koszty holowania. Przedmiotowe koszty mogą być pokryte przez Wynajmującego z wniesionej kaucji.</p>");

        sb.append("<p><strong>§ 3.</strong> Bieg okresu najmu rozpoczyna się od określonej w umowie najmu daty i godziny.</p>");

        sb.append("<p><strong>§ 4.</strong> W przypadku użytkowania przedmiotowego samochodu po dniu wygaśnięcia umowy Wynajmujący obciąży Najemcę karą umowną w wysokości 500 PLN za każdą rozpoczętą dobę wystawiając na rzecz Najemcy notę księgową. O bezumownym korzystaniu z samochodu zostaną powiadomione odpowiednie organy ścigania, w tym Policja.</p>");

        sb.append("<p><strong>§ 5.</strong> Okres wypowiedzenia umowy wynosi 3 miesiące.</p>");

        sb.append("<p><strong>§ 6.</strong> Najemca może wnioskować o przedłużenie umowy najmu pisemnie w formie maila (tomasz.kosinski@wp.pl) lub SMS na nr 506137418 nie później niż dzień przed datą wygaśnięcia umowy. Umowa zostaje przedłużona, jeżeli Wynajmujący wyrazi zgodę.</p>");

        sb.append("<h3>IV. Postępowanie w przypadku awarii i kradzieży</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> W przypadku wystąpienia jakiejkolwiek awarii technicznej przedmiotu najmu, Najemca/Kierowca zobowiązany jest do zabezpieczenia pojazdu oraz do niezwłocznego poinformowania Wynajmującego o zdarzeniu udzielając wszelkich informacji dotyczących jego stanu oraz miejsca jego postoju. Wynajmujący wskaże najbliższy autoryzowany serwis. Najemca ponosi koszty naprawy w serwisie wskazanym przez wynajmującego. Rozliczenie kosztów naprawy nastąpi w momencie zwrotu pojazdu Wynajmującemu na podstawie faktury wystawionej przez serwis na Wynajmującego. Czas najmu pojazdu zostaje przedłużony o czas pozostawania pojazdu w serwisie lub o czas dostarczenia pojazdu zastępczego. Wynajmujący nie ponosi odpowiedzialności za szkody poniesione przez najemcę w skutek awarii samochodu. Jeżeli awaria techniczna przedmiotu najmu wystąpiła bez winy Najemcy/Kierowcy, Wynajmujący może w miarę możliwości wynająć najemcy inny pojazd.</p>");
        sb.append("<p><strong>2.</strong> Najemca nie może dokonywać napraw przedmiotu najmu bez zgody i wiedzy Wynajmującego. Nie może również podejmować działań mogących stanowić zagrożenie dla bezpieczeństwa ruchu drogowego.</p>");

        sb.append("<p><strong>§ 2.</strong></p>");
        sb.append("<p><strong>1.</strong> W przypadku kradzieży przedmiotu najmu, zaistnienia zdarzenia drogowego z udziałem przedmiotu najmu Najemca/Kierowca zobowiązany jest wezwać Policję na miejsce zdarzenia, zażądać od Policji wydania kopii notatki, protokołu lub innego dokumentu potwierdzającego fakt zaistnienia któregokolwiek z przedmiotów zdarzeń.</p>");
        sb.append("<p><strong>2.</strong> Najemca zobowiązany jest również do poinformowania o opisanych powyżej zdarzeniach oraz niezwłocznego przekazania uzyskanych dokumentów oraz informacji, które będą Wynajmującemu do dochodzenia roszczeń powstałych w związku z opisanymi powyżej zdarzeniami.</p>");
        sb.append("<p><strong>3.</strong> Najemca zobowiązany jest do udzielenia powyższych informacji Wynajmującemu.</p>");

        sb.append("<p><strong>§ 3.</strong></p>");
        sb.append("<p><strong>1.</strong> W przypadku zawinionego przez Najemcę uszkodzenia samochodu kaucja Najemcy zostanie wstrzymana do czasu ustalenia kosztów czynności związanych z likwidacją naprawy. Po ustaleniu wysokości kosztów strony dokonają rozliczenia.</p>");
        sb.append("<p><strong>2.</strong> W przypadku gdy koszty przekraczają wysokość kaucji Najemca będzie zobowiązany do pokrycia powstałej nadwyżki, natomiast gdy koszty będą niższe od wysokości kaucji różnica zostanie zwrócona Najemcy.</p>");

        sb.append("<p><strong>§ 4.</strong> Zagubienie dokumentów lub kluczyków traktowane będzie jak złamanie niniejszej umowy najmu. Kaucja nie zostanie zwrócona, najemca poniesie pełny koszt odtworzenia dokumentów i (lub) kluczyków do pojazdu.</p>");

        sb.append("<h3>VII. Odpowiedzialność i opłaty</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Za wynajem pojazdu Najemca uiszcza opłatę z góry bądź na podstawie faktury VAT wystawionej przez Wynajmującego w dniu zawarcia umowy na rachunek bankowy Wynajmującego wskazany na fakturze.</p>");
        sb.append("<p><strong>2.</strong> Za datę zapłaty Strony uznają datę obciążenia rachunku bankowego Najemcy.</p>");

        sb.append("<p><strong>§ 2.</strong> W przewidzianych Umową wypadkach obciążenia karą umowną, Wynajmujący ma prawo żądać od Najemcy zapłaty odszkodowania uzupełniającego, jeżeli szkoda poniesiona przez Wynajmującego przewyższa wartość kary umownej.</p>");

        sb.append("<p><strong>§ 3.</strong> Wynajmujący nie jest odpowiedzialny wobec osób trzecich za jakiekolwiek roszczenia odszkodowawcze będące następstwem szkody spowodowanej przez Najemcę lub Kierowcę w okresie najmu.</p>");

        sb.append("<p><strong>§ 4.</strong> Jeżeli Najemca będzie zalegał w opłacaniu czynszu dłużej niż 7 dni, Wynajmujący wezwie Najemcę do zapłaty długu wyznaczając w tym zakresie termin nie dłuższy niż 3 dni. Po upływie tego terminu i braku uregulowania należności będzie to równoznaczne z rozwiązaniem umowy. Wynajmujący uprawniony będzie do odebrania pojazdu, co nie będzie stanowić zaboru wynajętego mienia. Kosztami odbioru zostanie obciążony Najemca.</p>");

        sb.append("<p><strong>§ 5.</strong> Samochód posiada ubezpieczenie na warunkach firmy ubezpieczeniowej w zakresie OC, AC, NW oraz Assistance. Najemca zobowiązany jest do zapoznania się oraz przestrzegania warunków umowy ubezpieczenia. W przypadku utraty pojazdu wraz z dowodem rejestracyjnym i (lub) kluczykami najemca ponosi pełną odpowiedzialność materialną. Złamanie warunków ubezpieczenia traktowane będzie jak złamanie warunków umowy najmu i skutkuje pełną odpowiedzialnością materialną wynajmującego. Najemca przyjmuje na siebie pełną odpowiedzialność materialną za szkody powstałe z własnej winy a nie objęte polisą AC. Za szkody objęte polisą AC użytkownik odpowiada do kwoty udziału własnego tj. 2000 zł. W przypadku szkód do 2000 zł, użytkownik ponosi pełny koszt naprawy pojazdu. Najemca ponosi pełny koszt naprawy uszkodzeń wnętrza (tapicerka, pasy bezpieczeństwa, radio, nawigacja, itp.) wynikających z winy najemcy a nie związanych z kolizją lub kradzieżą pojazdu. Wszelkie zaplamienia tapicerki powstałe w czasie najmu zostaną usunięte na koszt wynajmującego.</p>");

        sb.append("<h3>VIII. Postanowienia końcowe</h3>");

        sb.append("<p><strong>§ 1.</strong></p>");
        sb.append("<p><strong>1.</strong> Najemca oświadcza, że zapoznał się z umową najmu.</p>");
        sb.append("<p><strong>2.</strong> Najemca oświadcza również, iż zapoznał się z postanowieniami umowy oraz cennika i je akceptuje.</p>");

        sb.append("<p><strong>§ 2.</strong> Najemca upoważnia Wynajmującego do wystawienia Faktury VAT bez podpisu Najemcy zgodnie z postanowieniami umowy.</p>");

        sb.append("<p><strong>§ 3.</strong> Najemca oświadcza, że wyraża zgodę na przetwarzanie swoich danych osobowych przez Wynajmującego.</p>");

        sb.append("<p><strong>§ 4.</strong> Najemca potwierdza, że podane do Umowy dane są prawdziwe, wyraża zgodę na ich umieszczenie w bazie danych Wynajmującego, który będzie ich administratorem, na ich przekazywanie osobom trzecim, a także na ich przetwarzanie zgodnie z ustawą z dnia 29.08.1997 r. o ochronie danych osobowych (Dz. U. Nr 133 poz. 883) w celu wykonania Umowy, zabezpieczenia transakcji oraz w celach marketingowych.</p>");

        sb.append("<p><strong>§ 5.</strong> Najemca oświadcza, że został poinformowany o prawie wglądu do swoich danych i możliwości żądania uzupełnienia, uaktualnienia, sprostowania oraz czasowego lub stałego wstrzymania ich przetwarzania lub ich usunięcia.</p>");

        sb.append("<p><strong>§ 6.</strong> W sprawach nieuregulowanych w niniejszej umowie będą miały zastosowanie odpowiednie przepisy Kodeksu Cywilnego.</p>");

        sb.append("<p><strong>§ 7.</strong> Sądem właściwym do rozpoznania ewentualnych sporów wynikających z niniejszej umowy jest Sąd właściwy ze względu na miejsce siedziby Wynajmującego.</p>");

        sb.append("<p><strong>§ 8.</strong> Wszelkie zmiany niniejszej umowy będą dokonywane przez strony wyłącznie w formie pisemnej pod rygorem nieważności.</p>");

        sb.append("<p><strong>§ 9.</strong> Umowę sporządzono w dwóch jednakowych egzemplarzach po jednym dla każdej strony.</p>");
        sb.append("</div>");
        sb.append("<div class=\"section\">");
        sb.append("<h2>Obowiązek informacyjny z art. 13 RODO</h2>");
        sb.append("<p>Zgodnie z art. 13 rozporządzenia Parlamentu Europejskiego i Rady (UE) 2016/679 z dnia 27 kwietnia 2016 r. w sprawie ochrony osób fizycznych w związku z przetwarzaniem danych osobowych i w sprawie swobodnego przepływu takich danych oraz uchylenia dyrektywy 95/46/WE (tekst w języku polskim: Dziennik Urzędowy Unii Europejskiej, Nr 4.5.2016) (RODO), informujemy iż:</p>");
        sb.append("<p>Administratorem Pana/Pani danych osobowych jest LIDER S.C. ul. Krzywa 4a 08-530 Dęblin.</p>");
        sb.append("<p>Podstawę przetwarzania Pana/Pani danych osobowych stanowi wyrażona przez Pana/Panią zgoda.</p>");
        sb.append("<p>Administrator danych nie ma zamiaru przekazywać Pana/Pani danych osobowych do państwa trzeciego lub organizacji międzynarodowej, w tym również do takich w stosunku do których Komisja Europejska stwierdziła odpowiedni stopień ochrony.</p>");
        sb.append("<p>Okres, przez który dane osobowe będą przechowywane: do czasu odwołania zgody.</p>");
        sb.append("<p>Przysługuje Panu/Pani prawo do żądania od administratora danych dostępu do danych osobowych Pana/Pani dotyczących, ich sprostowania, usunięcia lub ograniczenia, a także o prawo do przenoszenia danych.</p>");
        sb.append("<p>Przysługuje Pani/Panu prawo do cofnięcia zgody w dowolnym momencie bez wpływu na zgodność z prawem przetwarzania, którego dokonano na podstawie zgody przed jej cofnięciem.</p>");
        sb.append("<p>Przysługuje Pani/Panu prawo do wniesienia skargi do polskiego organu nadzorczego lub organu nadzorczego innego państwa członkowskiego Unii Europejskiej, właściwego ze względu na miejsce zwykłego pobytu lub pracy lub ze względu na miejsce domniemanego naruszenia RODO.</p>");
        sb.append("<p>Podanie przez Panią/Pana danych osobowych jest dobrowolne, nie jest wymogiem ustawowym, umownym, lub warunkiem zawarcia umowy. Nie jest Pan/Pani zobowiązany do podania danych osobowych. Nie ma żadnych konsekwencji niepodania danych osobowych poza tym, iż w takim przypadku nie będą do Pana/Pani kierowane informacje marketingowe.</p>");
        sb.append("<p>W ramach prowadzenia działań marketingowych nie są podejmowane zautomatyzowane decyzje. Dane osobowe nie są profilowane.</p>");
        sb.append("<p>Zgoda może zostać cofnięta w dowolnym momencie, a wycofanie zgody nie wpływa na zgodność z prawem przetwarzania, którego dokonano na podstawie zgody przed jej wycofaniem.</p>");
        sb.append("</div>");

        sb.append("<table style=\"width: 100%; margin-top: 60px; border-collapse: collapse;\">");
        sb.append("<tr>");
        sb.append("<td style=\"width: 50%; text-align: left; vertical-align: top;\">");
        sb.append("<strong>Wynajmujący</strong><br/>");
        sb.append("<div style=\"border-top: 1px solid #000; width: 200px; margin-top: 5px; padding-top: 40px;\"></div>");
        sb.append("<p style=\"margin-top: 5px;\">LIDER S.C.</p>");
        sb.append("</td>");
        sb.append("<td style=\"width: 50%; text-align: right; vertical-align: top;\">");
        sb.append("<strong>Najemca</strong><br/>");
        sb.append("<div style=\"border-top: 1px solid #000; width: 200px; margin-top: 5px; padding-top: 40px;\"></div>");
        sb.append("<p style=\"margin-top: 5px;\">");
        sb.append(c.getFirstName() != null ? c.getFirstName() : "");
        sb.append(" ");
        sb.append(c.getLastName() != null ? c.getLastName() : "");
        sb.append("</p>");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");

        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }
}