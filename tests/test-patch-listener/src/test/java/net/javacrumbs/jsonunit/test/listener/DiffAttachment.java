package net.javacrumbs.jsonunit.test.listener;

import io.qameta.allure.attachment.AttachmentData;

public class DiffAttachment implements AttachmentData {
    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    private String patch;
    private String actual;
    private String expected;


    @Override
    public String getName() {
        return "JSON difference";
    }
}