# processors-server
A [`spray`](spray.io) server exposing a REST API for text annotation via the [`processors`](https://github.com/clulab/processors) library

## How is this useful?
This might be useful to people wanting to do NLP in a non-`JVM` language without a good existing parser.  Currently the only service uses `processors`' `FastNLPProcessor`, a wrapper for `CoreNLP`.


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
sbt "runMain NLPServer myfavoriteport"
```


# Communicating with the server
The examples that follow assume you started the server on port `8888`.

## Annotating text

### An example using `cURL`

POST `json` using `cuRL`.  The text to parse should be given as the value of the `json`'s `text` field:  
```bash
curl -H "Content-Type: application/json" -X POST -d '{"text": "My name is Inigo Montoya. You killed my father. Prepare to die."}' http://localhost:8888/annotate
```

```json
{
  "text": "My name is Inigo Montoya. You killed my father. Prepare to die.",
  "sentences": {
    "1": {
      "words": [
        "My",
        "name",
        "is",
        "Inigo",
        "Montoya",
        "."
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
      "dependencies": [
        {
          "incoming": 1,
          "outgoing": 0,
          "relation": "poss"
        },
        {
          "incoming": 4,
          "outgoing": 1,
          "relation": "nsubj"
        },
        {
          "incoming": 4,
          "outgoing": 2,
          "relation": "cop"
        },
        {
          "incoming": 4,
          "outgoing": 3,
          "relation": "nn"
        },
        {
          "incoming": 4,
          "outgoing": 5,
          "relation": "punct"
        }
      ]
    },
    "2": {
      "words": [
        "You",
        "killed",
        "my",
        "father",
        "."
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
      "dependencies": [
        {
          "incoming": 3,
          "outgoing": 2,
          "relation": "poss"
        },
        {
          "incoming": 1,
          "outgoing": 3,
          "relation": "dobj"
        },
        {
          "incoming": 1,
          "outgoing": 4,
          "relation": "punct"
        },
        {
          "incoming": 1,
          "outgoing": 0,
          "relation": "nsubj"
        }
      ]
    },
    "3": {
      "words": [
        "Prepare",
        "to",
        "die",
        "."
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
      "dependencies": [
        {
          "incoming": 0,
          "outgoing": 2,
          "relation": "xcomp"
        },
        {
          "incoming": 0,
          "outgoing": 3,
          "relation": "punct"
        },
        {
          "incoming": 2,
          "outgoing": 1,
          "relation": "aux"
        }
      ]
    }
  }
}
```

## Shutting down the server

You can shut down the server by posting anything to `/shutdown`

## Checking on server's status

send a `GET` to `/status`

# Future Plans
- Add service for `BioNLPProcessor`
- Offer fat `jar` + `Python` library
- Add service for running rule-based [`odin` rule-based ie](http://arxiv.org/pdf/1509.07513v1.pdf) on some built-in grammars
- Add service for submitting [`odin` rule-based ie](http://arxiv.org/pdf/1509.07513v1.pdf) custom grammars
- Smarter Actors
