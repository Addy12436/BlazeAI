package com.ai.service;

import org.springframework.stereotype.Service;

@Service
public class HtmlStorageService {

    private String latestGeneratedHtml;

    public String getLatestGeneratedHtml() {
        return latestGeneratedHtml;
    }

    public void setLatestGeneratedHtml(String html) {
        this.latestGeneratedHtml = html;
    }
}

