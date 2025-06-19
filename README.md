# Holybook

This repository contains the server backend and web frontend as well as several command-line utilities for the data
ingestion.

The following diagram gives an overview of the pipeline:

```mermaid
graph LR;
    i[fetch-index]-->c[fetch-content];
    c-->im[import];
    im-->db[postgres db];
    db-->s[ktor server];
    s-->w[react frontend];
```

## Index

The index is a helper data structure that specifies the list of documents available and where to find content for the
documents. The index is represented by simple text files. Each line represents one document and has the following space
separated entries:

```
<language code> <author id> <document id> <link to content>
```

Example of a message of the Universal House of Justice:

```
en uhj uhj20240419_001 https://bahai.org/library/authoritative-texts/the-universal-house-of-justice/messages/20240419_001/20240419_001.xhtml
```

This repository provides a command line utility to automatically retrieve indices from web pages. Run it locally with:

```bash
./gradlew :fetch-index:run --args="-o <output directory>"
```

By default, the output directory points to a sibling directory of this repository named `data`. It is usually a clone of
the `holybook/data` repository ensuring that indices are saved to that repository.

## Import

The book xmls can be imported into the database with the `:import` application. In order to import into the Google cloud
instance of the database make sure you authorize the client first.

```shell
gcloud auth application-default login
```

The application itself can be run like this:

```shell
./gradlew :import:run --args="-jdbc <JDBCURL>"
```

It also provides individual parameters to specify the host, port, username and password for standard jdbc urls.