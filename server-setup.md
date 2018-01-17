# Steps for setting up on Server

## installing `java`

See https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-get-on-ubuntu-16-04

```
sudo apt-get update
sudo apt-get install default-jre
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
```

## installing `bc`

Don't know what it does, but was required to get `sbt` to play nice

```
sudo apt-get install bc
```

## installing `sbt`

```
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install sbt
```

## installing `node.js`

```
curl -sL https://deb.nodesource.com/setup_9.x | sudo -E bash -
sudo apt-get install -y nodejs
```

## installing `npm`

run `npm -v` first to see if already installed


```
sudo npm install npm -g
```

## ensure permissions

```
sudo chown -R mcapizzi processors-server
```

## adding `.sbtopts`
```
-J-Xms4G
-J-Xmx6G
```

# Running on server

```
sbt
run-main NLPServer --host 0.0.0.0
```

Then from local machine, use this `API` address: `http://[server_ip]:8888/api/annotate`
