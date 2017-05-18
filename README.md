# kt-starbound
A Starbound file format reading library for Kotlin and Java
## Disclaimer(s)
__This library is an early prototype, and should be used with extra caution!__

__A lot of the information for this library was found using the [py-starbound](https://github.com/blixt/py-starbound) repository! Please check it out if you're interested in a Python or Go implementation. I do not claim credit for reverse engineering the Starbound file formats.__

__I will be attempting to both contribute to py-starbound's format documentation, as well as write my own. At the moment, there is no complete documentation on Starbound's file formats.__
## Why?
There are very few libraries for reading Starbound files. The two that I've found the best were written by the same person, [blixt](https://github.com/blixt), and even their own documentations of Starbound's file formats were sometimes lacking. In order to both discover the file format of Starbound's files and to provide a generic library for working with them, I wrote kt-starbound.
### Why Kotlin?
The initial prototype of the library was in pure Java. However, Kotlin appears to be a very nice language, and works excellently with Java. It's got a lot of convenience features like preventing a lot of boilerplate code (like data classes) and has the ability to check for null-related issues with the type system. This library was partially written to try out the language itself, and is my first attempt at writing Kotlin.
## Usage
### BTreeDB5
Currently, the kt-starbound provides three separate classes that represent Starbound file formats. The first is BTreeDB5. This is the most algorithmically complicated format, and if you want to understand how it works I encourage you to take a look into [B-Trees](https://en.wikipedia.org/wiki/B-tree). The format's most important application is in the `.world ` files. The library provides a class that reads a generic BTreeDB5 file, `BTreeDB5` (surprise) as well as a separate class, `World`, that handles some functions related to `.world` files, like extracting some metadata.

At the moment, a `World` instance is created as such:
```Java
World world = new World(new File("/path/to/file.world"));
```

Or, if you're using Kotlin:
```Kotlin
val world = World(File("/path/to/file.world"))
```

From there, at the moment, you can only use the `getMetadata()` function (or just `metadata` if you're using Kotlin).
### SBAsset6
The SBAsset file is used for `packed.pak` and all other mod files. It's fairly primitive, and the library provides an `SBAsset6` class that takes a Java file and parses it. The library currently uses RandomAccessFile, which gives it the ability to read even large asset files without much overhead.

Below is an example of printing out all the files in a packed asset file:
```Java
SBAsset6 asset = new SBAsset6(new File("/path/to/packed.pak"));
asset.getFiles().keySet().forEach(System.out::println);
```
### SBVJ06
What appears to be a serialized JSON format, this format is also fairly simple. Just like JSON, it has several data types, including ones that can be nested. It's used frequently in many places, like player data, and a similar type of encoding is used in world files. kt-starbound provides a Dynamic class and several children (`DynamicDouble`, `DynamicString`, etc), as well as a `SBVJ06` class to read this kind of file. 

The `SBVJ06` class's most important field is `versionedData`, which is more common encoding type across all of Starbound's file formats (SBVJ06 is always a whole file, while VersionedData is simply serialized JSON that can be found inside of other data structures). 

The `VersionedData` class has a `root` variable, which represents the top-level Dynamic object. The type of the object can then be checked with `instanceof` (or `when .. is` in Kotlin), and after casting, its value can be accessed using `getData()`. A simple example of accessing data from an SBVJ06 file is:
```Java
SBVJ06 jv = new SBVJ06(new File("/path/to/sbasset"));
DynamicMap rootElement = (DynamicMap) jv.getVersionedData().getRootElement();
rootElement.getData().keySet().forEach(System.out::println);
```