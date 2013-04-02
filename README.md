QuickSort Parallelo
=================== 

Questo programma in Java esegue un ordinamento di un file TSV, tramite una versione parallela dell'algoritmo quicksort totalmente in RAM (quindi è limitato dalla quantità di memoria della macchina).

L'ordinamento è effettuato in base a un campo, il cui indice è indicato come argomento dalla linea di comando.

Il confronto varia in base al Locale, che è applicato al livelo TERTIARY.

Utilizzo
========

L'applicazione richiede 4 parametri, più il locale che è opzionale (se non specificato usa 'en-US'):

* Il percorso del file TSV in input

* L'indice (da 0 in su) della colonna in base alla quale ordinare

* Il numero di thread (1-12) da usare

* Il percorso del file TSV prodotto in output

* Opzionalmente, il locale (per esempio per il Lituano è *ln-LN*)

Funzionamento
=============

Il programma istanzia il numero richiesto di thread e pone in una coda gli indici, iniziale e finale, della zona dell'array su cui operare, inizialmente tutto.

Un thread consuma un elemento della coda, applica un'iterazione e inserisce nella coda le coppie di indici corrispondenti alle partizioni ottenute dividendo in base al pivot.

Il pivot è scelto selezionando 3 elementi a caso dalla sezione di array da esaminare e facendone la mediana.

Se la partizione ha dimensione inferiore a 100, il thread utilizza la ricorsione senza parallelizzare.
