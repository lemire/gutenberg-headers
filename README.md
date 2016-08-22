# Removing Manually-Generated Boilerplate from Project Gutenberg e-Books

We consider the Project Gutenberg corpus (https://www.gutenberg.org/), where most documents are in ASCII format with preambles and epilogues that are often copied and pasted or manually typed. For applications such as machine learning, it is desirable to automatically remove this boilerplate text.


Thankfully, this can be automated  with a simple statistical approach.


Reference: Owen Kaser and Daniel Lemire, Removing Manually-Generated Boilerplate from Electronic Texts: Experiments with Project Gutenberg e-Books. CASCON 2007, pp. 272-275, 2007. http://arxiv.org/abs/0707.1913

## License

This code is in the public domain.


## Requirement

- Java
- A dump of Gutenberg text files (e.g., see http://www.gutenberg.org/wiki/Gutenberg:The_CD_and_DVD_Project)


## Purpose

This code is a research prototype. It is not meant for end-users.


## Usage


Make a symlink called ``data`` to wherever the (flattened) Gutenberg
text files are.  They should all be in one huge directory.  Beware of
duplicate names.  It processes those files in the directory whose name
matches the Java regexp string "((.*[012][0-9][a-z]?)|(\\d+))\\.txt"

The main class is called Gut. A reasonable way to run it
is

```
java Gut -l -f -h
```

It will produce some slightly verbose output, including the algorithm's
guess as to the last line number for the header and the first line number
of the epilogue.

The -e flag could be used if running on a system with enough memory.  It
will use exact counts rather than memory-saving approximate techniques.
Can see source for ``Gut.main()`` for some other command-line arguments.

One could postprocess this verbose output to recover just the filenames and line
numbers.   Or a non-verbose flag could be added in ``main()``.

It would also be reasonable (for human checking) to print the next few
nonblank line(s) after the header supposedly ends and the first few
nonblank line(s) before the epilogue supposedly begins.  This in not
currently done.

Example use (using just a few dozen test files in the ``extext03`` directory
 from the PG CDROM compilation - so there are not enough files for good
training.)

```bash
$ cd GutHeadersSrc
$ javac Gut.java
$ ln -s /media/owen/PGCD0803/etext03/ data
$ java Gut -l -f -h
assuming the text files are in 'data'
c is 100000
BookKeeper created: GenMajority@177daaa
Gut created
Scanning file data/8ataw11.txt
Scanning file data/8clln10.txt
Scanning file data/8fmtm10.txt
Scanning file data/8frml10.txt
Scanning file data/8jrny10.txt
Scanning file data/8rran10.txt
Scanning file data/bough11.txt
Scanning file data/bygdv10.txt
Scanning file data/chacr10.txt
Scanning file data/cprrn10.txt
Scanning file data/crtcm10.txt
Scanning file data/cwgen11.txt
Scanning file data/dchla10.txt
Scanning file data/dkang10.txt
Scanning file data/emohh10.txt
Scanning file data/eotsw10.txt
Scanning file data/fgths10.txt
Scanning file data/fkchp10.txt
Scanning file data/gbwlc10.txt
Scanning file data/gn06v10.txt
Scanning file data/hgrkr10.txt
Scanning file data/hhmms11.txt
Scanning file data/jj13b10.txt
Scanning file data/jpnft10.txt
Scanning file data/lchms10.txt
Scanning file data/lfcpn10.txt
Scanning file data/mjbrb10.txt
Scanning file data/mtlad10.txt
Scanning file data/niebl10.txt
Scanning file data/nnchr10.txt
Scanning file data/nplnb10.txt
Scanning file data/nqpmr10.txt
Scanning file data/nsnvl10.txt
Scanning file data/pmbrb10.txt
Scanning file data/ponye10.txt
Scanning file data/prtrt10.txt
Scanning file data/pygml10.txt
Scanning file data/rcddv10.txt
Scanning file data/rosry11.txt
Scanning file data/rplan10.txt
Scanning file data/shlyc10.txt
Scanning file data/sp85g10.txt
Scanning file data/tfdbt10.txt
Scanning file data/thdcm10.txt
Scanning file data/thjwl10.txt
Scanning file data/thngl10.txt
Scanning file data/trthn10.txt
Scanning file data/twtp110.txt
Scanning file data/twtp210.txt
Scanning file data/twtp410.txt
Scanning file data/ulyss12.txt
Scanning file data/vbgle11a.txt
Scanning file data/vfear11a.txt
Scanning file data/vlpnr10.txt
Scanning file data/wmnlv10.txt
at value 2 I have added 98996 fakes and theory bound is 1.3479565204347956
suggested threshold for GenMajority: 1 (c=100000, l = true)
File: data/8ataw11.txt
last header:line 354: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 14955: End of The Project Gutenberg Etext of The Antediluvian World
File: data/8clln10.txt
last header:line 368: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 16989: End of Project Gutenberg's Etext of The Autobiography of Benvenuto Cellini
File: data/8fmtm10.txt
last header:line 358: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 5846: End of The Project Gutenberg Etext of Famous Men of The Middle Ages
File: data/8frml10.txt
last header:line 69: tells you about restrictions in how the file may be used.
first footer:line 7612: More information about this book is at the top of this file.
File: data/8jrny10.txt
last header:line 366: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 10365: End of The Project Gutenberg Etext of A Journey to the Interior of the Earth
File: data/8rran10.txt
last header:line 351: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 18397: End of The Project Gutenberg Etext of The Adventures of Roderick Random
File: data/bough11.txt
last header:line 39: *** START OF THE PROJECT GUTENBERG EBOOK THE GOLDEN BOUGH ***
first footer:line 37505: End of Project Gutenberg's The Golden Bough, by Sir James George Frazer
File: data/bygdv10.txt
last header:line 374: INFORMATION ABOUT THIS E-TEXT EDITION
first footer:line 7075: End of Project Gutenberg's Beyond Good and Evil, by Friedrich Nietzsche
File: data/chacr10.txt
last header:line 375: Sue Asscher <asschers@dingoblue.net.au>
first footer:line 6852: End of The Project Gutenberg Etext of Chaucer, by Adolphus William Ward
File: data/cprrn10.txt
last header:line 352: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 21644: End of Project Gutenberg's The Critique of Pure Reason, by Immanuel Kant
File: data/crtcm10.txt
last header:line 368: software or any other related product without express permission.]
first footer:line 3911: End of The Project Gutenberg Etext of Cartrefi Cymru, by Owen M. Edwards
File: data/cwgen11.txt
last header:line 47: *** START OF THE PROJECT GUTENBERG EBOOK MEMOIRS THREE CIVIL WAR GENERALS***
first footer:line 87899: *** END OF THE PROJECT GUTENBERG EBOOK OF THREE CIVIL WAR GENERALS ***
File: data/dchla10.txt
last header:line 354: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 8487: End of The Project Gutenberg Etext of David Crockett: His Life and Adventures
File: data/dkang10.txt
last header:line 364: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/12/01*END*
first footer:line 3946: End of The Project Gutenberg Etext of Dot and the Kangaroo, by Ethel Pedley
File: data/emohh10.txt
last header:line 375: Amy E Zelmer <a.zelmer@cqu.edu.au>
first footer:line 9008: End of Project Gutenberg Etext of Every Man Out Of His Humour, by Ben Jonson
File: data/eotsw10.txt
last header:line 369: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 10200: End of The Project Gutenberg Etext of Epicoene: Or, The Silent Woman
File: data/fgths10.txt
last header:line 353: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 837: End of the Project Gutenberg Etext of A few Figs from Thistles
File: data/fkchp10.txt
last header:line 69: tells you about restrictions in how the file may be used.
first footer:line 14043: Charles Franks and the Online Distributed Proofreading Team.
File: data/gbwlc10.txt
last header:line 360: This etext was produced by Eve Sobol, South Bend, Indiana, USA
first footer:line 22431: End of Project Gutenberg's The Golden Bowl, Complete, by Henry James
File: data/gn06v10.txt
last header:line 72: This etext was produced by David Widger <widger@cecomet.net>
first footer:line 306397: This etext was produced by David Widger <widger@cecomet.net>
File: data/hgrkr10.txt
last header:line 353: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 5656: End of the Project Gutenberg Etext of A History Of Greek Art, by F. B. Tarbell
File: data/hhmms11.txt
last header:line 349: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 7597: End of The Project Gutenberg eText of Haydn, by J. Cuthbert Hadden***
File: data/jj13b10.txt
last header:line 372: This etext was produced by David Widger <widger@cecomet.net>
first footer:line 24739: End of this Project Gutenberg Etext of The Confessions of Rousseau, v12
File: data/jpnft10.txt
last header:line 375: Online Distributed Proofreading Team.
first footer:line 8194: End of Project Gutenberg's Japanese Fairy Tales, by Yei Theodora Ozaki
File: data/lchms10.txt
last header:line 380: Sue Asscher <asschers@dingoblue.net.au>
first footer:line 11519: End of this Project Gutenberg Etext of The Alchemist, by Ben Jonson.
File: data/lfcpn10.txt
last header:line 391: INFORMATION ABOUT THIS E-TEXT EDITION
first footer:line 5570: End of the Project Gutenberg Etext of Life of Chopin, by Franz Liszt
File: data/mjbrb10.txt
last header:line 382: This etext was produced by Eve Sobol, South Bend, Indiana, USA
first footer:line 5084: The End of the Project Gutenberg Etext of Major Barbara by
File: data/mtlad10.txt
last header:line 352: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 10507: End of the Project Gutenberg Etext of Mutual Aid, by P. Kropotkin
File: data/niebl10.txt
last header:line 363: software or any other related product without express permission.]
first footer:line : FAILURE OF FOOTER ALGORITHM (or, no footer)
File: data/nnchr10.txt
last header:line 376: Online Distributed Proofreading Team.
first footer:line 3746: End of Project Gutenberg's Anna Christie, by Eugene O'Neill
File: data/nplnb10.txt
last header:line 365: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 5311: End of The Project Gutenberg Etext of Napoleon Bonaparte
File: data/nqpmr10.txt
last header:line 377: INFORMATION ABOUT THIS E-TEXT EDITION
first footer:line 5770: End of Project Gutenberg's An Enquiry Concerning the Principles of Morals
File: data/nsnvl10.txt
last header:line 68: tells you about restrictions in how the file may be used.
first footer:line 4615: More information about this book is at the top of this file.
File: data/pmbrb10.txt
last header:line 382: This etext was produced by Eve Sobol, South Bend, Indiana, USA
first footer:line 1859: End of The Project Gutenberg Etext of Preface to Major Barbara:
File: data/ponye10.txt
last header:line 61: tells you about restrictions in how the file may be used.
first footer:line 2798: More information about this book is at the top of this file.
File: data/prtrt10.txt
last header:line 353: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 10292: End of the Project Gutenberg Etext of A Portrait of the Artist as a Young Man
File: data/pygml10.txt
last header:line 371: This etext was produced by Eve Sobol, South Bend, Indiana, USA
first footer:line 4973: End of The Project Gutenberg Etext of Pygmalion, by George Bernard Shaw
File: data/rcddv10.txt
last header:line 368: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 8876: End of Project Gutenberg's Arcadian Adventures With the Idle Rich, by Leacock
File: data/rosry11.txt
last header:line 372: Online Distributed Proofreading Team.
first footer:line 12361: End of Project Gutenberg Etext of The Rosary, by Florence L. Barclay
File: data/rplan10.txt
last header:line 354: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 12604: End of The Project Gutenberg Etext of The Red Planet
File: data/shlyc10.txt
last header:line 42: *** START OF THE PROJECT GUTENBERG ETEXT THE COMPLETE POETICAL WORKS ***
first footer:line 59267: End of the Project Gutenberg EBook of The Complete Poetical Works of Percy
File: data/sp85g10.txt
last header:line 358: This etext was produced by David Widger <widger@cecomet.net>
first footer:line 113297: End of this Project Gutenberg Etext of The Diary of Samuel Pepys, complete
File: data/tfdbt10.txt
last header:line 365: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 18238: End of The Project Gutenberg Etext of The Fifteen Decisive Battles of
File: data/thdcm10.txt
last header:line 369: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 11595: End of The Project Gutenberg Etext of The Decameron, Volume I
File: data/thjwl10.txt
last header:line 366: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 9178: End of The Project Gutenberg Etext of The Jewel of Seven Stars
File: data/thngl10.txt
last header:line 353: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 9485: End of The Project Gutenberg Etext of The English Constitution
File: data/trthn10.txt
last header:line 67: tells you about restrictions in how the file may be used.
first footer:line 21207: More information about this book is at the top of this file.
File: data/twtp110.txt
last header:line 366: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 8047: End of The Project Gutenberg Etext of The Writings of Thomas Paine Vol. I
File: data/twtp210.txt
last header:line 366: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 10069: End of The Project Gutenberg Etext of The Writings of Thomas Paine Vol. II
File: data/twtp410.txt
last header:line 366: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 7448: End of The Project Gutenberg Etext of The Writings of Thomas Paine Vol. IV
File: data/ulyss12.txt
last header:line 47: *** START OF THE PROJECT GUTENBERG EBOOK ULYSSES ***
first footer:line 32437: End of the Project Gutenberg EBook of Ulysses, by James Joyce
File: data/vbgle11a.txt
last header:line 369: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.07/27/01*END*
first footer:line 23229: End of The Project Gutenberg Etext The Voyage of the Beagle, by
File: data/vfear11a.txt
last header:line 372: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.02/09/01*END*
first footer:line 7702: End of Project Gutenberg Etext The Valley of Fear, by Arthur Conan Doyle
File: data/vlpnr10.txt
last header:line 384: Sue Asscher <asschers@dingoblue.net.au>
first footer:line 11224: End of The Project Gutenberg Etext of Volpone; Or, The Fox
File: data/wmnlv10.txt
last header:line 353: *END THE SMALL PRINT! FOR PUBLIC DOMAIN ETEXTS*Ver.10/04/01*END*
first footer:line 24097: End of the Project Gutenberg Etext of Women in Love, by D.H. Lawrence
Group delta 0:77306
Group delta 1:21690
Group delta 1:252
Group delta 1:360
Group delta 1:26
Group delta 1:38
Group delta 1:18
Group delta 1:1
Group delta 1:1
Group delta 2:22
Group delta 4:2
Group delta 1:9
Group delta 1:9
Group delta 1:1
Group delta 1:2
Group delta 2:8
Group delta 1:3
Group delta 1:2
Group delta 2:1
Group delta 1:7
Group delta 1:17
Group delta 1:19
Group delta 1:20
Group delta 1:5
Group delta 1:18
Group delta 10:1
Group delta 3:1
Group delta 1:4
Group delta 1:3
Group delta 4:1
Group delta 1:4
Group delta 1:32
Group delta 1:2
Group delta 1:2
Group delta 1:7
Group delta 1:14
Group delta 1:89
Group delta 1:1
Group delta 21:1
Group delta 34:1
stat counters:
 b1=35801
 b2=305
 b3=23098
 b4=26849
 b5=112
 b6=232
 b7=35457
 c1=0
 c2=0
 c3=0
 p1=35801
 p2=22694
 p3=0
```
