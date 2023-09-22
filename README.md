# Subscribe Link Generator for Proxy Clients
[![Build Status](https://travis-ci.com/zhenghaoz/subscribe-link-generator.svg?branch=master)](https://travis-ci.com/zhenghaoz/subscribe-link-generator)
[![License](https://img.shields.io/github/license/zhenghaoz/subscribe-link-generator)]()

## Description
A simple Spring-Boot application that generates a subscribe link with base64 encoded content for proxy clients.
Proxy clients can use this link to fetch a list of proxy connection details from the server.
This proxy list is stored and read from a file which you need to specify in the VM options at project startup.
All the existing subscriptions are stored in an H2 persistent database.
The subscribe link has a validity period and will be expired after the period that you specified.

## Requirements
- Java 17
- Gradle 7.2 (optional)
- Docker (optional)

