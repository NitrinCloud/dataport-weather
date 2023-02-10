package de.dataport.weather.views.weather;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.dataport.weather.data.entity.SampleAddress;
import de.dataport.weather.data.service.SampleAddressService;
import de.dataport.weather.views.MainLayout;

import java.time.Duration;

@PageTitle("Weather")
@Route(value = "", layout = MainLayout.class)
public class WeatherView extends Div {

    private final TextField postalCode = new TextField("Postal code");
    private final TextField city = new TextField("City");
    private final TextField state = new TextField("State");
    private final TextField country = new TextField("Country");

    private final Button cancel = new Button("Clear");
    private final Button search = new Button("Search");

    private final TextField latitudeField = new TextField("Latitude");
    private final TextField longitudeField = new TextField("Longitude");
    private final TextField timezoneField = new TextField("Timezone");
    private final TextField temperatureField = new TextField("Temperature");
    private final TextField cloudsField = new TextField("Clouds");
    private final TextField windSpeedField = new TextField("Wind Speed");

    private final Binder<SampleAddress> binder = new Binder<>(SampleAddress.class);

    public WeatherView(SampleAddressService addressService) {
        addClassName("weather-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());
        add(createResultLayout());

        binder.bindInstanceFields(this);

        clearForm();

        cancel.addClickListener(e -> clearForm());
        search.addClickListener(e -> {
            JsonObject weather = addressService.getWeather(binder.getBean());

            if (weather == null || weather.get("cod") != null) {
                Notification notification = new Notification("Error", 5000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

                notification.open();
            } else {
                latitudeField.setValue(weather.get("lat").getAsString());
                longitudeField.setValue(weather.get("lon").getAsString());
                timezoneField.setValue(weather.get("timezone").getAsString());

                JsonObject current = weather.get("current").getAsJsonObject();
                double temp = current.get("temp").getAsDouble();
                temperatureField.setValue(String.valueOf(temp));

                int clouds = current.get("clouds").getAsInt();
                cloudsField.setValue(String.valueOf(clouds));

                double windSpeed = current.get("wind_speed").getAsDouble();
                windSpeedField.setValue(String.valueOf(windSpeed));
            }
        });
    }

    private Component createTitle() {
        return new H3("Address");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        postalCode.setAllowedCharPattern("\\d");
        formLayout.add(postalCode, city, state, country);
        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        search.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(search);
        buttonLayout.add(cancel);
        return buttonLayout;
    }

    private Component createResultLayout() {
        VerticalLayout parentLayout = new VerticalLayout();
        parentLayout.add(new H2("Result"));
        HorizontalLayout resultLayout = new HorizontalLayout();
        parentLayout.add(resultLayout);

        VerticalLayout zoneLayout = new VerticalLayout();
        latitudeField.setEnabled(false);
        zoneLayout.add(latitudeField);
        longitudeField.setEnabled(false);
        zoneLayout.add(longitudeField);
        timezoneField.setEnabled(false);
        zoneLayout.add(timezoneField);

        VerticalLayout dataLayout = new VerticalLayout();
        temperatureField.setEnabled(false);
        dataLayout.add(temperatureField);
        cloudsField.setEnabled(false);
        dataLayout.add(cloudsField);
        windSpeedField.setEnabled(false);
        dataLayout.add(windSpeedField);

        resultLayout.add(zoneLayout);
        resultLayout.add(dataLayout);
        return parentLayout;
    }

    private void clearForm() {
        this.binder.setBean(new SampleAddress());
    }
}
