package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private TimeUnit timeUnit;
    private int requestLimit;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    public static class Description{
        String participantInn;
        public Description(String participantInn){
            this.participantInn = participantInn;
        }
    }

    public static class Product{
        String certificate_document;
        String certificate_document_date;
        String certificate_document_number;
        String owner_inn;
        String producer_inn;
        String production_date;
        String tnved_code;
        String uit_code;
        String uitu_code;

        public Product(String certificate_document, String certificate_document_date,
                       String certificate_document_number, String owner_inn,
                       String producer_inn, String production_date,
                       String tnved_code, String uit_code,
                       String uitu_code
        ) {
            this.certificate_document = certificate_document;
            this.certificate_document_date = certificate_document_date;
            this.certificate_document_number = certificate_document_number;
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.tnved_code = tnved_code;
            this.uit_code = uit_code;
            this.uitu_code = uitu_code;
        }
    }

    public static class Document{
        Description description;
        String doc_id;
        String doc_status;
        String doc_type;
        boolean importRequest;
        String owner_inn;
        String participant_inn;
        String producer_inn;
        String production_date;
        String production_type;
        List<Product> products;

        public Document(Description description, String doc_id, String doc_status,
                        String doc_type, boolean importRequest, String owner_inn,
                        String participant_inn, String producer_inn, String production_date,
                        String production_type, List<Product> products
        ) {
            this.description = description;
            this.doc_id = doc_id;
            this.doc_status = doc_status;
            this.doc_type = doc_type;
            this.importRequest = importRequest;
            this.owner_inn = owner_inn;
            this.participant_inn = participant_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.production_type = production_type;
            this.products = products;
        }
    }
    public void createDocument(Document document, String signature) throws InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        //Список для отслеживания количества обращений к API
        List<Instant> timeArray = new ArrayList<>();

        //Создаём json файл из объекта Document
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(document);

        //Создаём Post запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        while (true)
        {
            if(timeArray.size()<requestLimit) {
                timeArray.add(Instant.now());
            } else if (Instant.now().toEpochMilli() - timeArray.get(0).toEpochMilli() > timeUnit.toMillis(1)) {
                timeArray.remove(0);
                timeArray.add(Instant.now());
            } else {
                TimeUnit.MILLISECONDS.sleep(timeUnit.toMillis(1)/requestLimit);
                continue;
            }

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(response.statusCode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {

        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 5);

        CrptApi.Description description = new Description("string");

        CrptApi.Product product = new Product("string",
                "2020-01-23",
                "string",
                "string",
                "string",
                "2020-01-23",
                "string",
                "string",
                "string"
        );

        List<Product> productList = new ArrayList<>();
        productList.add(product);

        CrptApi.Document document = new Document(
                description,
                "string",
                "string",
                "LP_INTRODUCE_GOODS",
                true,
                "string",
                "string",
                "string",
                "2020-01-23",
                "string",
                productList
        );

        crptApi.createDocument(document, "signature");
    }
}
