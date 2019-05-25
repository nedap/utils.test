# utils.test [![CircleCI](https://circleci.com/gh/nedap/utils.test.svg?style=svg&circle-token=40d5b1ddb5290559200d8569aeeba8ef70ef1883)](https://circleci.com/gh/nedap/utils.test)

A collection of test helpers.

## Synopsis

`nedap.utils.test.api/simplify` allows you to transform complex data for ease in comparison in tests. 
It transforms `Sequential` collections into sets and `IPersistentMap` into plain maps.

The [api test](test/unit/nedap/utils/test/api.cljc) shows a couple examples with records.

## Installation

```clojure
[com.nedap.staffing-solutions/utils.test "0.1.0-alpha1"]
```

## ns organisation

There is exactly 1 namespace meant for public consumption:
 - nedap.utils.test.api

## Documentation

Please browse the public namespaces, which are documented, speced and tested.

## License

Copyright © Nedap

This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
