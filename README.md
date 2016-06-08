# processors-server
A [`spray`](spray.io) server exposing a REST API for text annotation via the [`processors`](https://github.com/clulab/processors) library

## How is this useful?
This might be useful to people wanting to do NLP in a non-`JVM` language without a good existing parser.  Currently there are services for using `processors`' `FastNLPProcessor` (a wrapper for `CoreNLP`) and `BioNLPProcessor`.


## Running `processors-server`

```
git clone https://github.com/myedibleenso/processors-server.git
```

Fire up the server.  This may take a minute or so to load the large model files.

```
cd processors-server
sbt "runMain NLPServer"
```

By default, the server will run on port `8888`, though you can start the server using a different port:

```
sbt "runMain NLPServer <your favorite port here>"
```


# Communicating with the server
The examples that follow assume you started the server on port `8888`.

## Annotating text

### Available services

Text can be annotated by posting `json` with a `"text"` field to one of the following services.

#### `FastNLPProcessor`

- `http://localhost:<your port here>/annotate`
- `http://localhost:<your port here>/fastnlp/annotate`

#### `BioNLPProcessor`

- `http://localhost:<your port here>/bionlp/annotate`


### An example using `cURL`

To see it in action, you can try to POST `json` using `cuRL`.  The text to parse should be given as the value of the `json`'s `text` field:  
```curl -H "Content-Type: application/json" -X POST -d '{"text": "My name is Inigo Montoya. You killed my father. Prepare to die."}' http://localhost:8888/annotate```

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

## Shutting down the server

You can shut down the server by posting anything to `/shutdown`

## Checking on the server's status

send a `GET` to `/status`

# Other Stuff
If you're a Python user, you may be interested in using [`py-processors`](https://github.com/myedibleenso/py-processors) in your NLP project.

# Where can I get the latest and greatest fat `jar`?
Cloning the project and running `sbt assembly` ensures the latest `jar`.  You can download a recent tub-of-`jar` using one of these commands:

```bash
wget http://www.cs.arizona.edu/~hahnpowell/processors-server/current/processors-server.jar
```
or

```bash
curl -H "Accept: application/zip" http://www.cs.arizona.edu/~hahnpowell/processors-server/current/processors-server.jar -o processors-server.jar
```

# Future Plans
- Add service for running rule-based [`odin` rule-based ie](http://arxiv.org/pdf/1509.07513v1.pdf) on some built-in grammars
- Add service for submitting [`odin` rule-based ie](http://arxiv.org/pdf/1509.07513v1.pdf) custom grammars
- Smarter Actors
