[![Build Status](https://travis-ci.org/myedibleenso/processors-server.svg?branch=master)](https://travis-ci.org/myedibleenso/processors-server)

Current version: 2.7

# processors-server

## What is it?

A [`spray`](spray.io) server exposing a REST API for text annotation via the [`processors`](https://github.com/clulab/processors) library

## Requirements
1. [Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
2. [`sbt`](http://www.scala-sbt.org/download.html)

## How is this useful?
This might be useful to people wanting to do NLP in a non-`JVM` language without a good existing parser.  Currently there are services for using `processors`' `FastNLPProcessor` (a wrapper for `CoreNLP`) and `BioNLPProcessor`.

## Running `processors-server`

```bash
git clone https://github.com/myedibleenso/processors-server.git
```

Fire up the server.  This may take a minute or so to load the large model files.

```bash
cd processors-server
sbt "runMain NLPServer"
```

By default, the server will run on port `8888`, though you can start the server using a different port:

```bash
sbt "runMain NLPServer <your favorite port here>"
```

# Communicating with the server

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

You may also send text already segmented into sentences by posting a [`SentencesMessage`](src/main/resources/json/schema/sentences-message.json) (see [example](src/main/resources/json/examples/sentences-message.json)) to the same `annotate` endpoint.  This is just a `json` frame with a `"sentences"` field pointing to an array of strings.

#### `FastNLPProcessor`

- `http://localhost:<your port here>/annotate`
- `http://localhost:<your port here>/fastnlp/annotate`

#### `BioNLPProcessor`

Text can be annotated by sending a POST request containing `json` with a `"text"` field to the following endpoint (see [example](src/main/resources/json/examples/message.json)):

- `http://localhost:<your port here>/bionlp/annotate`

### Sentiment analysis with `CoreNLP`

- `http://localhost:<your port here>/corenlp/sentiment/score`
  - Requires one of the following`json` POST requests:
    - [`Document`](src/main/resources/json/schema/document.json) (see [example](src/main/resources/json/examples/document.json))
    - [`Sentence`](src/main/resources/json/schema/sentence.json) (see [example](src/main/resources/json/examples/sentence.json))
    - [`Message`](src/main/resources/json/schema/message.json) (see [example](src/main/resources/json/examples/message.json))

You can also send text that has already been segmented into sentences:
  - post a [`SentencesMessage`](src/main/resources/json/schema/sentences-message.json) (see [example](src/main/resources/json/examples/sentences-message.json)) to `http://localhost:<your port here>/corenlp/sentiment/score/segmented`

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

A `POST` to an `/annotate` endpoint will return a `Document` of the form specified in [`document.json`](src/main/resources/json/schema/document.json).

### An example using `cURL`

To see it in action, you can try to `POST` `json` using `cuRL`.  The text to parse should be given as the value of the `json`'s `text` field:   
```bash
curl -H "Content-Type: application/json" -X POST -d '{"text": "My name is Inigo Montoya. You killed my father. Prepare to die."}' http://localhost:8888/annotate
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

## Checking on the server's status

send a `GET` to `/status`

# `py-processors`
If you're a Python user, you may be interested in using [`py-processors`](https://github.com/myedibleenso/py-processors) in your NLP project.

# Where can I get the latest and greatest fat `jar`?
Cloning the project and running `sbt assembly` ensures the latest `jar`.  You can download a recent tub-of-`jar` [here](http://www.cs.arizona.edu/~hahnpowell/processors-server/current/processors-server.jar).
