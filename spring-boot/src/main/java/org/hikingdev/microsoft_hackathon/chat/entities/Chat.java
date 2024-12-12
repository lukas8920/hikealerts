package org.hikingdev.microsoft_hackathon.chat.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Chat {
    private final String supported_countries = "US, NZ & IE.";
    private final String maxTextError = "The description may not exceed 200 words. Please try to summarize your input.";
    private final String minTextError = "The description must be at least 5 words long. Please try again.";
    private final String intro = "Hi, let me know, if you are aware of a hiking alert.";
    private final String login = "Please log in first to use the chat";
    private final String instruction = "Let me know, if you would like to delete or insert an alert. Please describe the situation, name the trail and country";
    private final String inputEvaluation = "Thanks for your input. Please be patient, evaluating input...";
    private final String trailNameError = "I was not able to identify a trail name from your input. Feel free to write your alert with a trail name again.";
    private final String countryError = "I can not add this alert. Currently, we only support alerts for " + supported_countries;
    private final String noCountryError = "I was not able to identify a country from your input. Feel free to write me with a country again.";
    private final String searchingGeodata = "Searching trail geodata...";
    private final String searchingPatience = "Please be patient while I check against the trail database...";
    private final String searchError = "I was not able to identify the trail geodata for this input. Feel free to write your alert with a trail name again.";
    private final String alertSuccess = "The alert is now visible. Thanks for providing the data.";
    private final String deleteRequest = "5b) Trying to delete alert...";
    private final String noAlertError = "I was not able to find the alert. Feel free trying again to delete an alert with a trail name.";
    private final String ownerError = "You are not the owner of the alert. You can only delete alerts issued through your user. Feel free to delete an alert with a trail name again.";
    private final String deleteSuccess = "The alert has been deleted. Thanks for the update.";
    private final String notProcessable = "I was not able to process your response, please try again.";
}
