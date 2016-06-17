# processors-server
A [`spray`](spray.io) server exposing a REST API for text annotation via the [`processors`](https://github.com/clulab/processors) library

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

### Available services

Text can be annotated by posting `json` with a `"text"` field to one of the following services  (see [example](src/main/resources/json/examples/message.json)).

#### `FastNLPProcessor`

- `http://localhost:<your port here>/annotate`
- `http://localhost:<your port here>/fastnlp/annotate`

#### `BioNLPProcessor`

- `http://localhost:<your port here>/bionlp/annotate`

#### Sentiment analysis with `CoreNLP`

- `http://localhost:<your port here>/corenlp/sentiment/document`
  - Requires a `json` request containing a [`Document`](src/main/resources/json/schema/document.json) (see [example](src/main/resources/json/examples/document.json))
- `http://localhost:<your port here>/corenlp/sentiment/sentence`
  - Requires a `json` request containing a [`Sentence`](src/main/resources/json/schema/sentence.json) (see [example](src/main/resources/json/examples/sentence.json))
- `http://localhost:<your port here>/corenlp/sentiment/text`
  - Requires a `json` request containing a [`Message`](src/main/resources/json/schema/message.json) (see [example](src/main/resources/json/examples/message.json))

# Responses

The a POST to a `/annotate` endpoint will return a `json Document` of the form specified in [`document.json`](src/main/resources/json/schema.document.json).

### An example using `cURL`

To see it in action, you can try to POST `json` using `cuRL`.  The text to parse should be given as the value of the `json`'s `text` field:   
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

# `json` schema for responses

Response schema can be found at (`src/main/resources/json/schema`)[src/main/resources/json/schema]

Examples of each can be found at (`src/main/resources/json/examples`)[src/main/resources/json/examples]


## Shutting down the server

You can shut down the server by posting anything to `/shutdown`

## Checking on the server's status

send a `GET` to `/status`

# Other Stuff
If you're a Python user, you may be interested in using [`py-processors`](https://github.com/myedibleenso/py-processors) in your NLP project.

# Where can I get the latest and greatest fat `jar`?
Cloning the project and running `sbt assembly` ensures the latest `jar`.  You can download a recent tub-of-`jar` [here](http://www.cs.arizona.edu/~hahnpowell/processors-server/current/processors-server.jar).

# Future Plans
- Add service for running rule-based [`odin` rule-based ie](http://arxiv.org/pdf/1509.07513v1.pdf) on some built-in grammars
- Add service for submitting [`odin` rule-based ie](http://arxiv.org/pdf/1509.07513v1.pdf) custom grammars
- Smarter Actors
