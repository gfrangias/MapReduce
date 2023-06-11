import sys

def map_word_count():
    
    input_string = sys.stdin.read()

    words = input_string.split()

    for word in words:
        print(word, 1)

def map_character_count():

    input_string = sys.stdin.read()

    words = input_string.split()

    for word in words:
        for i in range(len(word)):
            print(word[i], 1)

def reduce_word_count():

    for line in sys.stdin:
        parts = line.strip().split()

        key = parts[0]
        values = list(map(int, parts[1:]))

        result = sum(values)

        print(key, result)

functions = {
    "map_word_count":map_word_count,
    "map_character_count":map_character_count,
    "reduce_word_count":reduce_word_count
}

if __name__ == "__main__":

    try:
        functions[sys.argv[1]]()

    except KeyError:

        print('Function does not exist...')
        exit(1)
