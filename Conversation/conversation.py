import nltk
import calendar
import difflib
import re
from num2words import num2words

months = [calendar.month_name[i].lower() for i in range(1,13)]
days = [calendar.day_name[i].lower() for i in range(0,7)]
months_abbr = [calendar.month_abbr[i].lower() for i in range(1,13)]
days_abbr = [calendar.day_abbr[i].lower() for i in range(0,7)]
start_preps = ['at','on','from','between','after','post']
end_preps = ['at','before','to','pre','and']
number_words = {num2words(i).lower().replace('-',' '):i for i in range(1,59)}

def match(value,lst_values):
    """ Test for close matches
    >>> match('decembr',months)
    ['december', 'september']
    >>> match('februry',months)
    ['february']
    """
    return difflib.get_close_matches(value,lst_values)

def remove_alpha(value):
    """ Test for remove alphabets
    >>> remove_alpha('12qert4')
    '124'
    >>> remove_alpha('30th')
    '30'
    """
    return re.sub("[^0-9]", "", value)

def is_month(word):
    return len(match(word,months)) > 0 or len(match(word,months_abbr)) > 0

def get_date(tagged,index):
    ret_val = None
    word,tag = tagged[index]
    if tag == 'CD':
        date_val = remove_alpha(word)
        if len(date_val) > 0 and int(date_val) > 0 and int(date_val) < 32:
            ret_val = date_val
    return ret_val

def find_valid_date(index,tagged):
    ret_val = None
    for i in range(index-1,0,-1):
        date = get_date(tagged,i)
        if date <> None:
            ret_val = date
            break
    if ret_val is None:
        for i in range(index+1,len(tagged)):
            date = get_date(tagged,i)
            if date <> None:
                ret_val = date
                break
    return ret_val

def find_month(word):
    mth = match(word,months)
    ret_val = 0
    if len(mth) > 0:
        ret_val = months.index(mth[0])+1
    else:
        ret_val = months_abbr.index(match(word,months_abbr)[0])+1
    return ret_val

def is_day(word):
    return len(match(word,days)) > 0 or len(match(word,days_abbr))

def find_day(word):
    ret_val = match(word,days)
    if len(ret_val) > 0:
        ret_val = ret_val[0]
    else:
        ret_val = match(word,days_abbr)[0]
    return ret_val

def populate_day_or_date(tagged,index,word,ret_val):
    if is_month(word) and 'date' not in ret_val:
        date = find_valid_date(index,tagged)
        if date <> None:
            ret_val['date']='{:02d}/{:02d}/2016'.format(find_month(word),int(date)) # Hardcoded year for now
        else:
            ret_val['date']='{:02d}/??/2016'.format(find_month(word))
    elif is_day(word) and 'day' not in ret_val:
        ret_val['day']=find_day(word)

def is_not_month_or_day(tagged,indices):
    for index in indices:
        word,tag = tagged[index]
        if (is_month(word.lower()) or is_day(word.lower())) and (tag == 'NNP' or tag == 'NN'):
            return False
    return True

def check_for_time_value(word,tagged,index):
    return ((word.isdigit() and int(word) < 24) or (remove_alpha(word).isdigit() and int(remove_alpha(word)) < 24) or (word in number_words and number_words[word] < 24 )) \
        and is_not_month_or_day(tagged,[index+1,index+2])

def is_time(word,tagged,index):
    for i in range(index+1,len(tagged)):
        word,tag = tagged[i]
        if (tag == 'CD' or tag=='NN') and check_for_time_value(word.lower(),tagged,index):
            return True
    return False


def is_start_time(word,tagged,index):
    return is_time(word,tagged,index) and len(match(word,start_preps)) > 0
    
def is_end_time(word,tagged,index):
    return is_time(word,tagged,index) and len(match(word,end_preps)) > 0

def convert_to_time(word,next_word):
    ret_val = int(remove_alpha(word)) if word.lower() not in number_words else number_words[word.lower()]
    if ('pm' in word.lower() or  'pm' in next_word.lower()) and ret_val <> 12 :
        ret_val+=12
    return ret_val

def get_time(word,tagged,index):
    for i in range(index+1,len(tagged)):
        word,tag = tagged[i]
        if (tag == 'CD' or tag == 'NN') and check_for_time_value(word.lower(),tagged,index):
            next_word,next_tag = tagged[i+1]
            return convert_to_time(word.lower(),next_word)
    return None

def populate_time(tagged,index,word,ret_val):
    if is_start_time(word,tagged,index) and 'startTime' not in ret_val:
        ret_val['startTime']=get_time(word,tagged,index)
    if is_end_time(word,tagged,index) and 'endTime' not in ret_val:
        ret_val['endTime']=get_time(word,tagged,index)
            

def parse_sentence(sentence):
    """ Test the parsing of the sentence
    >>> parse_sentence('Is there a slot available on Sunday post 2 PM?')
    {'date': '??/SUN/2016', 'startTime': 14}
    >>> parse_sentence('Could you offer a slot between 9 and 12 on 30th December')
    {'date': '12/30/2016', 'endTime': 12, 'startTime': 9}
    >>> parse_sentence('before 10 AM. Is it possible?')
    {'endTime': 10}
    >>> parse_sentence('Is there a slot available on Sunday after 1PM?')
    {'date': '??/SUN/2016', 'startTime': 13}
    >>> parse_sentence('Is there a slot available on Tuesday before twelve PM?')
    {'date': '??/TUE/2016', 'endTime': 12}
    >>> parse_sentence('Can we meet at 7 PM ?')
    {'endTime': 19, 'startTime': 19}
    """
    ret_val=dict()
    tagged = nltk.pos_tag(nltk.word_tokenize(sentence))
    for index,token in enumerate(tagged):
        word,tag = token
        if tag == 'NNP' or tag == 'NN': # Proper Noun : Could be a day or a month, or a mis-spelled noun
            populate_day_or_date(tagged,index,word.lower(),ret_val)
        elif tag == 'IN' or ('startTime' in ret_val and tag == 'CC') or word.lower() == 'post': # Chance of this being a time
            populate_time(tagged,index,word.lower(),ret_val)
    if 'day' in ret_val:
        my_date = '??/??/2016'
        if 'date' in ret_val:
            my_date = ret_val['date']
        existing_date_vals = my_date.split('/')
        ret_val['date']='{}/{}/2016'.format(existing_date_vals[0],ret_val['day'][0:3].upper())
        del ret_val['day']
    return ret_val
                    

if __name__=='__main__':
    import doctest
    doctest.testmod()    
