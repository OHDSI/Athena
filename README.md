#1 Phrase search

Search provides the ability to search by phrase. All results are sorted by default according to the following criteria:

 -full phrase match 
- concepts contain all the words from the search phrase
- result based on two parameters, the number of searched words in the result and importance of each word (importance is calculated for each word, the words that are rearer among all documents are more important)

Example:

Search phrase: **honey eats pooh**

Name | sort priority explanation |
--- | ---|
honey eats Pooh | full match  |
Pooh eats honey | all words |
Pooh eats raspberries and honey | 3 words |
Pooh steals honey | 2 words |
pooh eats |2 word |
pooh eats pooh |2 word |
pooh eats nothing |2 words |
Pooh eats raspberries |2 words |
Pooh eats raspberries and me | 2 words |
Piglet hates honey | 1 words |
pooh | 1 words |
Pooh | 1 words |
pooo | 1 words |


NB: the search goes through all concept fields, but the highest priority is given to CONCEPT_NAME and CONCEPT_CODE

#2 Exact search

Using quotation marks forces an exact-match search. 

For an exact search, the following conditions are met
- the word must be present
- not case sensitive, the number of spaces between words does not matter
- stemming is disabled(the word/words must be present exactly as it is in quotation marks )

Example 1:

Search phrase: **"Pooh eats honey"**

Name |
--- | 
Pooh eats honey |

Example 1:

Search phrase:  **"Pooh eats" honey**

Name |
--- |
Pooh eats honey |
Pooh eats raspberries and honey |
pooh eats |
pooh eats nothing |
pooh eats pooh |
Pooh eats raspberries |
Pooh eats raspberries and me |


#3 Special symbols

For special symbols, the following conditions are met
- If the character is not part of the word, it is ignored.
- These characters are ignored: / ? ! , | ; | \ .
- For all other characters, the first funded result will be with characters and then without

Search phrase: **[piglet]**

Name |
--- |
[piglet] loves balloon |
[Piglet] loves balloon |
[piglet loves balloon |
piglet] loves balloon |
(piglet) loves balloon |
{piglet} loves balloon |
(piglet loves balloon |
piglet) loves balloon |
piglet} loves balloon |
{piglet loves balloon |
Piglet hates honey |
piglet loves balloo |
piglet loves balloon |


A special character becomes mandatory if the word is surrounded by quotation marks.

Search phrase:  **"[piglet]"**

Name |
--- |
[piglet] loves balloon |
[Piglet] loves balloon |


#4 Approximate matching(fuzzy searching)

In case of a typo, or if there is a similar spelling of the word, the most similar result will be found

Search phrase: **Poo8 ets honny**

Name |
--- | 
honey eats Pooh|
Pooh eats honey|
Pooh eats raspberries and honey|
Pooh steals honey|
pooh eats pooh|
pooh eats|
pooh eats nothing|
Pooh eats raspberries|
Piglet hates honey|
Pooh eats raspberries and me|
pooh|
Pooh|
pooo|