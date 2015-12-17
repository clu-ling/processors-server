# processors-server
A [`spray`](spray.io) server exposing a REST API for text annotation via the [`processors`](https://github.com/clulab/processors) library

## How is this useful?
This might be useful to people wanting to do NLP in a non-`JVM` language without a good existing parser.  Currently the only service uses `processors`' `FastNLPProcessor`, a wrapper for `CoreNLP`.

## An example using `cURL`

1. Start the server.  
```sbt "runMain NLPServer"```
This may a minute as several large models files are loaded

2. POST `json` using `cuRL`.  The text to parse should be given as the value of the `json`'s `text` field:  
```curl -H "Content-Type: application/json" -X POST -d '{"text": "My name is Inigo Montoya. You killed my father. Prepare to die."}' http://localhost:8888/parse```

```
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

## Future Plans
- Add service for `BioNLPProcessor`
- Offer fat `jar` + `Python` library
- Add service for running rule-based [`odin` rule-based ie](http://arxiv.org/pdf/1509.07513v1.pdf) on some built-in grammars
- Add service for submitting [`odin` rule-based ie](http://arxiv.org/pdf/1509.07513v1.pdf) custom grammars
- Smarter Actors
