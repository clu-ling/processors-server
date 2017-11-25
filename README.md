[![Build Status](https://travis-ci.org/myedibleenso/processors-server.svg?branch=master)](https://travis-ci.org/myedibleenso/processors-server)

Current version: 3.1.0

# processors-server

## What is it?

An `akka-http` server exposing a REST API for text annotation via the [`processors`](https://github.com/clulab/processors) library

## Requirements
1. [Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
2. [`sbt`](http://www.scala-sbt.org/download.html)
3. [`node.js`](https://nodejs.org/en/)
4. [`npm`](https://www.npmjs.com/get-npm)

## How is this useful?
This might be useful to people wanting to do NLP in a non-`JVM` language without a good existing parser.  Currently there are services for using `processors`' `CluProcessor`, `FastNLPProcessor` (a wrapper for `CoreNLP`) and `BioNLPProcessor`.

## Running `processors-server`

```bash
git clone https://github.com/myedibleenso/processors-server.git
```

Fire up the server.  This may take a minute or so to load the large model files.

```bash
cd processors-server
sbt "runMain NLPServer"
```

By default, the server will run on port `8888` and `localhost`, though you can start the server using a different port and host:

```bash
sbt "runMain NLPServer --host <your favorite host here> --port <your favorite port here>"
```

## Building a docker container

```
sbt docker
```

This will create a container named `myedibleenso/processors-server:latest`, which you can run with `docker-compose up` using the included `docker-compose.yml` file.

You can find all of the official containers published on Docker Hub for this project in [this repo](https://hub.docker.com/r/myedibleenso/processors-server/). 

## Logging

A server log is written to `processors-server.log` in home directory of the user who launches the server.

# Communicating with the server

_NOTE: Once the server has started, a summary of the services currently available (including links to demos) can be found at the following url: `http://<your host name here>:<your port here>`

## Annotating text

The following services are available:

1. Text annotation (open-domain or biomedical) involving:
  - sentence splitting
  - tokenization
  - lemmatization
  - PoS tagging
  - NER
  - dependency parsing
2. Sentiment analysis
3. Rule-based IE using `Odin`

Text can be annotated by sending a POST request containing `json` with a `"text"` field to one of the following `annotate` endpoints (see [example](src/main/resources/json/examples/message.json)).

You may also send text already segmented into sentences by posting a [`SegmentedMessage`](src/main/resources/json/schema/segmented-message.json) (see [example](src/main/resources/json/examples/segmented-message.json)) to the same `annotate` endpoint.  This is just a `json` frame with a `"sentences"` field pointing to an array of strings.

#### `CluProcessor`

- `http://localhost:<your port here>/api/clu/annotate`

#### `FastNLPProcessor`

- `http://localhost:<your port here>/api/annotate`
- `http://localhost:<your port here>/api/fastnlp/annotate`

#### `BioNLPProcessor`

The resources (model files) for this processor are loaded lazily when the first call is made.

Text can be annotated by sending a POST request containing `json` with a `"text"` field to the following endpoint (see [example](src/main/resources/json/examples/message.json)):

- `http://localhost:<your port here>/api/bionlp/annotate`

### Sentiment analysis with `CoreNLP`

- `http://localhost:<your port here>/api/corenlp/sentiment/score`
  - Requires one of the following`json` POST requests:
    - [`Document`](src/main/resources/json/schema/document.json) (see [example](src/main/resources/json/examples/document.json))
    - [`Sentence`](src/main/resources/json/schema/sentence.json) (see [example](src/main/resources/json/examples/sentence.json))
    - [`Message`](src/main/resources/json/schema/message.json) (see [example](src/main/resources/json/examples/message.json))

You can also send text that has already been segmented into sentences:
  - post a [`SegmentedMessage`](src/main/resources/json/schema/segmented-message.json) (see [example](src/main/resources/json/examples/segmented-message.json)) to `http://localhost:<your port here>/api/corenlp/sentiment/score/segmented`

Responses will be [`SentimentScores`](src/main/resources/json/schema/scores.json) (see [example](src/main/resources/json/examples/scores.json))

### Rule-based IE with `Odin`

- `http://localhost:<your port here>/odin/extract`
  - Requires one of the following`json` POST requests:
    - [text with rules](src/main/resources/json/schema/text-with-rules.json) (see [example](src/main/resources/json/examples/text-with-rules.json))
    - [text with rules url](src/main/resources/json/schema/text-with-rules-url.json) (see [example](src/main/resources/json/examples/text-with-rules-url.json))
    - [document with rules](src/main/resources/json/schema/document-with-rules.json) (see [example](src/main/resources/json/examples/document-with-rules.json))
    - [document with rules url](src/main/resources/json/schema/document-with-rules-url.json) (see [example](src/main/resources/json/examples/document-with-rules-url.json))

For more info on `Odin`, see [the manual](http://arxiv.org/pdf/1509.07513v1.pdf)
# Responses

A `POST` to an `/api/annotate` endpoint will return a `Document` of the form specified in [`document.json`](src/main/resources/json/schema/document.json).

### An example using `cURL`

To see it in action, you can try to `POST` `json` using `cuRL`.  The text to parse should be given as the value of the `json`'s `text` field:   
```bash
curl -H "Content-Type: application/json" -X POST -d '{"text": "My name is Inigo Montoya. You killed my father. Prepare to die."}' http://localhost:8888/api/annotate
```

```json
{
  "text": "My name is Inigo Montoya. You killed my father. Prepare to die.",
  "sentences": [
    {
      "words": [
        "My",
        "name",
        "is",
        "Inigo",
        "Montoya",
        "."
      ],
      "startOffsets": [
        0,
        3,
        8,
        11,
        17,
        24
      ],
      "endOffsets": [
        2,
        7,
        10,
        16,
        24,
        25
      ],
      "lemmas": [
        "my",
        "name",
        "be",
        "Inigo",
        "Montoya",
        "."
      ],
      "tags": [
        "PRP$",
        "NN",
        "VBZ",
        "NNP",
        "NNP",
        "."
      ],
      "entities": [
        "O",
        "O",
        "O",
        "PERSON",
        "PERSON",
        "O"
      ],
      "dependencies": {
        "edges": [
          {
            "destination": 0,
            "source": 1,
            "relation": "poss"
          },
          {
            "destination": 1,
            "source": 4,
            "relation": "nsubj"
          },
          {
            "destination": 2,
            "source": 4,
            "relation": "cop"
          },
          {
            "destination": 3,
            "source": 4,
            "relation": "nn"
          },
          {
            "destination": 5,
            "source": 4,
            "relation": "punct"
          }
        ],
        "roots": [
          4
        ]
      }
    },
    {
      "words": [
        "You",
        "killed",
        "my",
        "father",
        "."
      ],
      "startOffsets": [
        26,
        30,
        37,
        40,
        46
      ],
      "endOffsets": [
        29,
        36,
        39,
        46,
        47
      ],
      "lemmas": [
        "you",
        "kill",
        "my",
        "father",
        "."
      ],
      "tags": [
        "PRP",
        "VBD",
        "PRP$",
        "NN",
        "."
      ],
      "entities": [
        "O",
        "O",
        "O",
        "O",
        "O"
      ],
      "dependencies": {
        "edges": [
          {
            "destination": 2,
            "source": 3,
            "relation": "poss"
          },
          {
            "destination": 3,
            "source": 1,
            "relation": "dobj"
          },
          {
            "destination": 4,
            "source": 1,
            "relation": "punct"
          },
          {
            "destination": 0,
            "source": 1,
            "relation": "nsubj"
          }
        ],
        "roots": [
          1
        ]
      }
    },
    {
      "words": [
        "Prepare",
        "to",
        "die",
        "."
      ],
      "startOffsets": [
        48,
        56,
        59,
        62
      ],
      "endOffsets": [
        55,
        58,
        62,
        63
      ],
      "lemmas": [
        "prepare",
        "to",
        "die",
        "."
      ],
      "tags": [
        "VB",
        "TO",
        "VB",
        "."
      ],
      "entities": [
        "O",
        "O",
        "O",
        "O"
      ],
      "dependencies": {
        "edges": [
          {
            "destination": 2,
            "source": 0,
            "relation": "xcomp"
          },
          {
            "destination": 3,
            "source": 0,
            "relation": "punct"
          },
          {
            "destination": 1,
            "source": 2,
            "relation": "aux"
          }
        ],
        "roots": [
          0
        ]
      }
    }
  ]
}
```

## `json` schema for responses

Response schema can be found at [`src/main/resources/json/schema`](src/main/resources/json/schema)

Examples of each can be found at [`src/main/resources/json/examples`](src/main/resources/json/examples)

# Other Stuff

## Shutting down the server

You can shut down the server by posting anything to `/shutdown`

## Checking the server's build

send a `GET` to `/buildinfo`

# `py-processors`
If you're a Python user, you may be interested in using [`py-processors`](https://github.com/myedibleenso/py-processors) in your NLP project.

# Where can I get the latest and greatest fat `jar`?
Cloning the project and running `sbt assembly` ensures the latest `jar`.  You can download a recent tub-of-`jar` [here](http://www.cs.arizona.edu/~hahnpowell/processors-server/current/processors-server.jar).
