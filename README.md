# Clojurians Log v2

This is the web app serving the historical archive of [Clojurians
Slack](https://clojurians.net/). It is the successor to the original
[clojurians-log-app](https://github.com/clojureverse/clojurians-log-app).

## Screenshots

![image](https://user-images.githubusercontent.com/4194289/129220923-e922ad97-43d0-4af6-9f30-9c3cf4d15054.png)

![image](https://user-images.githubusercontent.com/4194289/129221132-1f8e4594-828f-4b19-98ed-a47291e042b7.png)

## Development

This project uses the dev environment launcher
[Launchpad](https://github.com/lambdaisland/launchpad). It's the recommended way
to run the project locally, and get a nREPL connection into the main process.
See the Launchpad docs for how to use it with your particular editor.

Running through Launchpad also takes care of starting a PostgreSQL container.

Requirements: `clojure` CLI, `bb` (Babashka), `docker compose`

Example: start with the `dev` alias, include Emacs CIDER middleware for nREPL,
automatically connect CIDER, and run `(user/go)` to start the application.

```sh
./bin/launchpad dev --emacs --go
```

The app will run at http://localhost:9090. Run `(user/browse)` to open it in
your browser.

## `bin/dev`

There's a babashka script at `bin/dev` with dev tasks.

Create stub versions of 

```
bin/dev init
```

To get a PostgreSQL `psql` REPL 

``` sh
bin/dev psql
```

## `bin/slacklog`

Command line wrapper to run and manage the application. See `bin/slacklog --help`.

This is also the main way to get data in to the application. To get some stub
data in for testing, clone
[clojurians-log-demo-data](https://github.com/clojureverse/clojurians-log-demo-data),
then run

```
bin/slacklog import archive ../clojurians-log-demo-data
```

## Capturing Slack history

The accompanying
[https://github.com/clojureverse/slack-event-sink](slack-event-sink) is a
minimal application which will listen to Slack for new messages, and write them
out as newline-delimited JSON files, which can be imported with `bin/slacklog
import archive`.

It can also write them to a UNIX socket for immediate import, which is how it
interacts with clojurians-log-app-v2.

Alternatively, get a full workspace export through Slack's admin interface,
unzip it, and run `bin/slacklog import export <dir>`.

## Configuration

We use [lambdaisland/config](https://github.com/lambdaisland/config). The base
configuration lives in `resources/clojurians-log/config.edn`. You can locally
override these values by putting a `config.local.edn` in the root of the
repository. Environment variables or Java system properties are also supported,
see the documentation for `lambdaisland/config`.

## Contribute

Contributions and changes welcome! Don't worry about breaking things, we can
always fix it later, or revert and try again.

It can sometimes take a little while before I'll be able to get back to you.
Feel free to ping me (Arne/@plexus) on other channels (Clojurians Slack,
Fediverse (@plexus@toot.cat)) if you think I may have missed your issue or PR.
