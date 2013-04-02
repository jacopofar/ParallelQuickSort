QuickSort Parallelo
=================== 

Questo programma in Java esegue un ordinamento di un file TSV, tramite una versione parallela dell'algoritmo quicksort totalmente in RAM (quindi è limitato dalla quantità di memoria della macchina).

L'ordinamento è effettuato in base a un campo, il cui indice è indicato come argomento dalla linea di comando.

Il confronto varia in base al *Locale*, che è applicato al livello [TERTIARY](http://docs.oracle.com/javase/1.5.0/docs/api/java/text/Collator.html#setStrength(int)).

Utilizzo
========

L'applicazione richiede 4 parametri, più il locale che è opzionale (se non specificato usa 'en-US'):

* Il percorso del file TSV in input

* L'indice (da 0 in su) della colonna in base alla quale ordinare

* Il numero di thread (1-12) da usare

* Il percorso del file TSV che sarà prodotto in output

* Opzionalmente, il locale secondo la notazione ISO-639 (per esempio per il lituano è `lt`, per l'italiano `it`, [lista](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes))



Funzionamento
=============

Il programma istanzia il numero richiesto di thread e pone in una coda gli indici, iniziale e finale, della zona dell'array su cui operare, inizialmente tutto.

Un thread consuma un elemento della coda, applica un'iterazione e inserisce nella coda le coppie di indici corrispondenti alle partizioni ottenute dividendo in base al pivot.

Il pivot è scelto selezionando 3 elementi a caso dalla sezione di array da esaminare e facendone la mediana.

Se la partizione ha dimensione inferiore a 100, il thread utilizza la ricorsione senza parallelizzare.
