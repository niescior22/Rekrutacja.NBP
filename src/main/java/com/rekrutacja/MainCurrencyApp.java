package com.rekrutacja;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class MainCurrencyApp {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        String kodWaluty;

        String dataPoczątkowa;

        String dataKońcowa;

        System.out.println("Podaj kod waluty [EUR,USD,CHF,GBP]:");

        kodWaluty = scanner.nextLine();

        kodWaluty = kodWaluty.toLowerCase();
        System.out.println("Podaj datę początkową [YYYY-MM-DD]");

        dataPoczątkowa = scanner.nextLine();

        System.out.println("Podaj datę końcową [YYYY-MM-DD]");

        dataKońcowa = scanner.nextLine();


        try {
            //Klient rest api przy pomocy biblioteki Jersey
            Client klient = Client.create();

             //wysłanie zapytania typu get do klienta
            WebResource webResource = klient.resource("http://api.nbp.pl/api/exchangerates/rates/c/" + kodWaluty + "/" + dataPoczątkowa + "/" + dataKońcowa + "/");

            // utworzenie obiektu response wraz z okreslonym typem
            ClientResponse webResponse = webResource.accept("application/json").get(ClientResponse.class);

            //Weryfikacja połączenia, w przypadku niepowodzenia rzuca exc
            if (webResponse.getStatus() != 200) {
                throw new RuntimeException("Bład Http,  " + webResponse.getStatus());
            }

            //Otrzymanie odpowiedzi jako String i przypisanie do zmiennej
            String json = webResponse.getEntity(String.class);


            // Parsowanie Jasonba na POJO przy pomocy biblioteki Jackson
            ObjectMapper mapper = new ObjectMapper();

            ExchangeRates exchangeRates = mapper.readValue(json, ExchangeRates.class);

            List<Rate> rates = exchangeRates.getRates();
            //zaokrąglenie wynikow  metod na srednia bidow i odchylebie standardowe askow
            double bidMean=getMean(rates);
            bidMean *= 10000;
            bidMean=Math.round(bidMean);
            bidMean /= 10000;

            double askStandardDevotionFromPopulation= getStandartDevotion(rates);
            askStandardDevotionFromPopulation *= 10000;
            askStandardDevotionFromPopulation=Math.round(askStandardDevotionFromPopulation);
            askStandardDevotionFromPopulation/=10000;


            System.out.println(bidMean+" ---> sredni kurs kupna");
            System.out.println(askStandardDevotionFromPopulation+" --->odchylenie standardowe kursów sprzedaży");



        } catch (Exception exc) {
            exc.printStackTrace();
        }


    }
       //metoda licząca średnią bidow
    public static double getMean(List<Rate> rates) {
        double sum = 0.0;
        double mean = 0.0;
        for (int i = 0; i < rates.size(); i++) {
            sum += rates.get(i).getBid();
            mean = sum / rates.size();
        }
        return mean;
    }
    //metoda licząca srednią askow
    public static double getAskMean(List<Rate>rates){
        double sum =0.0;
        double mean = 0.0;
        for (int i = 0; i < rates.size(); i++) {
            sum += rates.get(i).getAsk();
            mean = sum / rates.size();
        }
        return mean;


    }
    //metoda licząca odchylenie standardowe askow
    public static  double getStandartDevotion(List<Rate> rates) {
        List<Double> listofdiffrences = new ArrayList<Double>();
        List<Double> squares = new ArrayList<Double>();
        double mean=getAskMean(rates);
        double sum =0;
        double var ;
        double result ;



        for(int i=0;i<rates.size();i++){
            double diffrence = rates.get(i).getAsk()- mean;
            listofdiffrences.add(diffrence);
        }
        for (int i=0;i<listofdiffrences.size();i++){
            double square = listofdiffrences.get(i)*listofdiffrences.get(i);
            squares.add(square);
        }

        for (int i =0;i<squares.size();i++){
            sum+=squares.get(i);

        }
        var = sum/(rates.size());

        result = Math.sqrt(var);
        return  result;
    }
}
