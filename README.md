# utils.test [![CircleCI](https://circleci.com/gh/nedap/utils.test.svg?style=svg&circle-token=40d5b1ddb5290559200d8569aeeba8ef70ef1883)](https://circleci.com/gh/nedap/utils.test)

A collection of test helpers.

## Synopsis

* `nedap.utils.test.api/meta=` compares both objects and their metadata, recursively.

* `nedap.utils.test.api/macroexpansion=` compares objects, deeming any gensyms as equal.

* `nedap.utils.test.api/run-tests` macroexpands to its clojure.test/cljs.test counterpart. It only adds something for the cljs variant: it sets an adequate exit code to the Node process.

* `nedap.utils.test.api/expect` allows you to assert side effects in code. look at [examples in the tests](https://github.com/nedap/utils.test/blob/55021bf884fb06aa3cb9d2706ffe6816a2923e45/test/unit/nedap/utils/test/api.cljc#L119-L123).

## Installation

```clojure
[com.nedap.staffing-solutions/utils.test "1.8.0"]
```

## ns organisation

 - `nedap.utils.test.api` 
 - `nedap.utils.test.matchers` matcher-combinators matchers

## Documentation

Please browse the public namespaces, which are documented, speced and tested.

## Development

The default namespace is `dev`. Under it, `(refresh)` is available, which should give you a basic "Reloaded workflow".

> It is recommended that you use `(clojure.tools.namespace.repl/refresh :after 'formatting-stack.core/format!)`.

You can find examples in the [api test](test/unit/nedap/utils/test/api.cljc).

## License

Copyright © Nedap

This program and the accompanying materials are made available under the terms of the [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0).
