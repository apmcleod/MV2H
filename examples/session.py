exit
ls
import librosa
import pyrubberband
import pyrubberband as pyrb
y, sr = librosa.load("../maas/extraction-examples/blues_00000.wav", sr=44100)
sr
y
pyrb.pitch_shift(y, sr, 1)
y = pyrb.pitch_shift(y, sr, 1)
librosa.write("../maas/extraction-examples/blues-shifted.wav")
import soundfile
soundfile.write("../maas/extraction-examples/blues-shifted.wav", y, 44100)
import tensorflow as tf
tf.__version__
import tensorflow
tensorflow.__version__
tensorflow.compat.v1 as tf
from tensorflow.compat import v1 as tf
import tensorflow.compat.v1 as tf
help(tf)
import tensorflow.v2.compat.v1
help(tf)
import numpy as np
e = 0.05
d = 0.01
thresholds = list(range(e, 1-e, d)
)
thresholds = np.arange(e, 1-e, d)
thresholds
thresholds = np.arange(e, 1-e+d, d)
thresholds
int((0.500002-e)/d)
thresholds[45]
thresholds[46]
import numpy as np
x = np.array([[0, 1], [2,3], [4,5]])
x
x[0]
x[0] > 0.5
np.mean(x, axis=0)
np.mean(x, axis=0) >= 2
np.mean(x, axis=0) >= 3
x = [[0,1],[2,3]]
x
np.array(x)
x[0]
x = [[0,1,2],[2,3]]
np.array(x)
import numpy as np
np.__version__
import maas
import librosa
import soundfile
import librosa
librosa.output.write_wav
from librosa import output
import librosa.output
import soundfile
import numpy as np
for i in [0.0, 0.2, 0.4, 0.6, 0.8, 1.0]:
  x += [i] * 5
x = []
for i in [0.0, 0.2, 0.4, 0.6, 0.8, 1.0]:
  x += [i] * 5
x
for i in [0.0, 0.2, 0.4, 0.6, 0.8, 1.0]:
  x += [i] * 5
x
x = x *= 10000
x = x * 10000
x
soundfile.write("test.wav", x, 48000)
import soundfile
x = []
for i in [0.0, 0.2, 0.4, 0.6, 0.8, 1.0]:
  x += [i] * 5
x = x * 10000
soundfile.write("test.wav", x, 24000)
import soundfile
x = list(range(-1.0, 1.0, 0.0001))
x += list(range(1.0, -1.0, -0.0001))
x = x * 100
import numpy as np
x = np.arange(-1.0, 1.0, 0.0001)
x += np.arange(1.0, -1.0, -0.0001)
x = x * 100
x
import numpy as np
x = list(np.arange(-1.0, 1.0, 0.0001))
x += list(np.arange(1.0, -1.0, -0.0001))
x = x * 100
x
soundfile.write("test-triangle.wav", x, 24000)
soundfile.write("test-offset.wav", [0.5] * 60 * 24000, 24000)
import numpy as np
np.zeros(24)
import src.utils.py
import "src/utils.py"
from src import utils
utils.chord_to_pitch_classes(12)
utils.CHORD_NOTES
utils.adjust_probabilities_given_melody_midi(
[1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
[(0,2,60),(0,1,50),(1,2,52)],
0,2)
import librosa
from src import utils
import pretty_midi as pm
import librosa
import numpy as np
librosa.hz_to_midi(60)
librosa.hz_to_midi([60, np.nan])
np.round(librosa.hz_to_midi([60, np.nan]))
np.round(librosa.hz_to_midi([60, np.nan]), int)
np.round(librosa.hz_to_midi([60, np.nan]), dtype=int)
np.round(librosa.hz_to_midi([60, np.nan]), 0)
np.round(librosa.hz_to_midi([60, np.nan]) + 0.5)
np.round(librosa.hz_to_midi([60, np.nan]) + 1)
np.round(librosa.hz_to_midi([60, np.nan]) - 0.5)
np.round(librosa.hz_to_midi([60, np.nan]) - 0.5).astype(int)
from src import transcription
transcription.get_contour_transcription("../../windows/Downloads/Heart to Heart Francine VOX.wav")
import importlib
importlib.refresh(transcription)
importlib.reload(transcription)
transcription.get_contour_transcription_as_midi("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.get_contour_transcription_as_midi("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.get_contour_transcription_as_midi("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.generate_midi_transcription("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.generate_midi_transcription("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.generate_midi_transcription("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.generate_midi_transcription("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.generate_midi_transcription("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.generate_midi_transcription("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.generate_midi_transcription("../../windows/Downloads/Heart to Heart Francine VOX.wav")
importlib.reload(transcription)
transcription.generate_midi_transcription("../../windows/Downloads/Heart to Heart Francine VOX.wav")
from src import generator
import generator
from src import generator
from generator import *
mood = "happy"
key = "C:maj"
start_chord = "C:maj"
beats_per_chord = 3
beats_per_chord = 4
input_file_path = "../../windows/Downloads/Santa Claus Is Coming to Town Francine Honey VOX.wav"
transition_matrix = np.load(            str(PROJECT_FOLDER / "data" / f"transition_matrix_{self.moods[mood]}.npy")        )
gen = Generator()
from src import generator
from generator import *
gen = Generator()
mood = "happy"
key = "C:maj"
start_chord = "C:maj"
beats_per_chord = 4
input_file_path = "../../windows/Downloads/Santa Claus Is Coming to Town Francine Honey VOX.wav"
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
from src import generator
from generator import *
gen = Generator()
mood = "happy"
key = "C:maj"
start_chord = "C:maj"
beats_per_chord = 4
input_file_path = "../../windows/Downloads/Santa Claus Is Coming to Town Francine Honey VOX.wav"
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
from src import generator
from generator import *
gen = Generator()
mood = "happy"
key = "E:maj"
start_chord = "E:maj"
beats_per_chord = 4
beats_per_chord = 2
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
input_file_path = "../../windows/Downloads/Santa Claus Is Coming to Town Francine Honey VOX.wav"
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
from src import generator
from generator import *
gen = Generator()
mood = "happy"
key = "E:maj"
start_chord = "E:maj"
beats_per_chord = 2
beats_per_chord = 4
input_file_path = "../../windows/Downloads/Santa Claus Is Coming to Town Francine Honey VOX.wav"
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
from src import generator
from generator import *
gen = Generator()
mood = "happy"
key = "E:maj"
start_chord = "E:maj"
beats_per_chord = 4
input_file_path = "../../windows/Downloads/Santa Claus Is Coming to Town Francine Honey VOX.wav"
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
from src import generator
from generator import *
mood = "happy"
gen = Generator()
key = "E:maj"
start_chord = "E:maj"
beats_per_chord = 4
input_file_path = "../../windows/Downloads/Santa Claus Is Coming to Town Francine Honey VOX.wav"
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
from src import generator
from generator import *
mood = "happy"
gen = Generator()
key = "E:maj"
start_chord = "E:maj"
beats_per_chord = 4
input_file_path = "../../windows/Downloads/Santa Claus Is Coming to Town Francine Honey VOX.wav"
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
from src import generator
from generator import *
mood = "happy"
mood = "Mixolydian"
key = "A#:maj"
start_chord = "G:min"
beats_per_chord = 4
input_file_path = "../../windows/Downloads/I Soldier On Francine Honey VOX-short.wav"
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
gen = Generator()
gen.audio_upload(input_file_path, mood, key, start_chord, beats_per_chord)
import numpy as np
np.__version__
import librosa
librosa.fft_frequencies(sr=48000, n_fft=4)
import scipy
from scipy.fft import fft
import numpy as np
x = np.array([-365,-355,234,90,-15,36,-3,46,-55,15,-9,-7,-30,-5,-10,-17,-4,-17,-4,-15,7,-22,-15,22,11,-98,84,9,46,-11,106,297])
x
fft(x)
x = x / (2 ** 15)
x
fft(x)
x * 2 ** 15
fft(x) * 2 ** 15
np.fft.rfft(x)
np.fft.rfft(x) * 2 ** 15
from scipy.fft import fftpack
from scipy import fftpack
fftpack.rfft(x)
fftpack.rfft(x) * 2 **15
x * 2 ** 15
x = x * 2 ** 15
x
fftpack.rfft(x)
np.fft.rfft(x)
np.sin(4)
np.sin(np.pi)
np.sin(x * np.pi)
np.sin(y * np.pi) for y in range(32)
[np.sin(y * np.pi) for y in range(32)]
[np.sin(y * np.pi / 2) for y in range(32)]
x = [np.sin(y * np.pi / 2) for y in range(32)]
x
x * 2 ** 32
x * 2 ** 15
x = [0, 32767, 0, -32768] * 8
x
len(x)
np.fft.rfft(x)
import maas
maas.config.CONFIG
maas.utils.CONFIG
from maas import utils
utils.CONFIG
utils.CONFIG["user"]
api_user = utils.CONFIG["user"]
with open(CONFIG_DIR / CONFIG["refresh_token_file"], "r") as file:
            api_refresh_token = file.read()
from maas.utils import CONFIG_DIR, CONFIG
with open(CONFIG_DIR / CONFIG["refresh_token_file"], "r") as file:
            api_refresh_token = file.read()
from maas.api_comm import *
import maas
api_user = utils.CONFIG["user"]
from maas.utils import CONFIG_DIR, CONFIG
api_user = CONFIG["user"]
with open(CONFIG_DIR / CONFIG["refresh_token_file"], "r") as file:
            api_refresh_token = file.read()
from maas.api_comm import *
with open(CONFIG_DIR / CONFIG["refresh_token_file"], "r") as file:
            api_refresh_token = file.read()
conn = JamahookApiConnection(api_user, refresh_token=api_refresh_token)
conn._get_wav_urls(["5f73883f2160d30aad7d146e"])
conn.get_wav_paths(["5f73883f2160d30aad7d146e"], Path("."))
import mir_eval
from mir_eval.transcription import precision_recall_f1_overlap as f1
import numpy as np
f1([[1,2]], [60], [[0.999, 1.000],[1.000,2.000]], [60,60], offset_ratio=None)
f1(np.array([[1,2]]), np.array([60]), np.array([[0.999, 1.000],[1.000,2.000]]), np.array([60,60]), offset_ratio=None)
from jamahook_eval.test_setup import *
enumerate_triplets("/home/mcl/windows/Downloads/User_Feedback.txt")
enumerate_triplets("/home/mcl/windows/Downloads/User_Feedback.txt", "Harmony")
enumerate_triplets("/home/mcl/windows/Downloads/User_Feedback1.txt", "Harmony")
enumerate_triplets("/home/mcl/windows/Downloads/User_Feedback1.txt", "Sound")
enumerate_triplets("/home/mcl/windows/Downloads/User_Feedback2.txt", "Sound")
from sample_selecter import *
load_all_patterns_from_directory("drum-patterns")
from pattern_selecter import *
load_all_patterns_from_directory("drum-patterns")
all_patterns = load_all_patterns_from_directory("drum-patterns")
all_patterns["clap"]
all_patterns["clap"][0]
all_patterns["clap"]['0']
all_patterns["clap"]['0'].get_formatted_string()
from pattern_selecter import *
all_patterns = load_all_patterns_from_directory("drum-patterns")
all_patterns["clap"]['0']
all_patterns["clap"]['0'].get_formatted_string()
all_patterns = load_all_patterns_from_directory("drum-patterns")
from pattern_selecter import *
all_patterns = load_all_patterns_from_directory("drum-patterns")
all_patterns["clap"]['0'].get_formatted_string()
all_patterns["clap"]['0'].get_formatted_string(longest_name_length=10)
all_patterns["clap"]['0'].get_formatted_string(longest_name_length=10, beat_separator="l")
all_patterns["clap"]['0'].get_formatted_string(longest_name_length=10)
all_patterns["combination"]
load_all_combinations_from_json("drum-patterns/combination.json")
load_all_combinations_from_json("drum-patterns/combination.json", all_patterns)
from pattern_selecter import *
all_patterns = load_all_patterns_from_directory("drum-patterns")
all_patterns
load_all_combinations_from_json("drum-patterns/combination.json", all_patterns)
combos = load_all_combinations_from_json("drum-patterns/combination.json", all_patterns)
combos
combos["techno"]
combos["techno"].get_formatted_string()
from pattern_selecter import *
all_patterns = load_all_patterns_from_directory("drum-patterns")
load_all_combinations_from_json("drum-patterns/combination.json", all_patterns)
combos = load_all_combinations_from_json("drum-patterns/combination.json", all_patterns)
combos["techno"]
combos["techno"].get_formatted_string()
from pattern_selecter import *
all_patterns = load_all_patterns_from_directory("drum-patterns")
combos = load_all_combinations_from_json("drum-patterns/combination.json", all_patterns)
combos["techno"].get_formatted_string()
print(combos["techno"].get_formatted_string())
from pattern_selecter import *
all_patterns = load_all_patterns_from_directory("drum-patterns")
combos = load_all_combinations_from_json("drum-patterns/combination.json", all_patterns)
combos["techno"].get_formatted_string()
print(combos["techno"].get_formatted_string())
import gradio
import maas
from maas.song_generaror import Song
import maas.song_generator
from maas.song_generator import Song
import numpy
import seaborn
import numpy
import numpy as np
np.__version__
seaborn.__version__
import pandas as pd
pd.__version__
import tqdm
import scipy
import librosa
import keras
from vector_utils import n_gram_transition_matrix
import numpy as np
x = np.arange(12)
n_gram_transition_matrix(x, 1, 12)
n_gram_transition_matrix(x, 2, 12)
from vector_utils import n_gram_transition_matrix
import numpy as np
x = np.arange(12)
n_gram_transition_matrix(x, 1, 12)
n_gram_transition_matrix(x, 2, 12)
from vector_utils import n_gram_transition_matrix
import numpy as np
x = np.arange(12)
n_gram_transition_matrix(x, 1, 12)
n_gram_transition_matrix(x, 2, 12)
n_gram_transition_matrix(x, 3, 12)
v = n_gram_transition_matrix(x, 3, 12)
v[0][1]
v[1][2]
v[1][1]
from vector_utils import *
x = np.arange(12)
v = n_gram_transition_matrix(x, 2, 12)
entropy(v)
from vector_utils import *
x = np.arange(12)
v = n_gram_transition_matrix(x, 2, 12)
entropy(v)
v
entropy(v)
entropy([0,1])
entropy([0,1,1])
entropy([0,0,0,1])
entropy([0,0,0,0])
x = [0,1,1,0,1,2]
n_gram_transition_matrix(x, 2, 3)
v=n_gram_transition_matrix(x, 2, 3)
entropy(v)
sparsity(v)
from vector_utils import *
x = [0,1,1,0,1,2]
v=n_gram_transition_matrix(x, 2, 3)
entropy(v)
sparsity(v)
v
np.linalg.norm(v, ord=1, axis=-1)
0 / (l2_norm+0.00001)
0 / (0+np.finfo(float).eps)
sparsity(v)
from vector_utils import *
get_max_entropy(4)
max_entropy(4)
max_entropy(2)
entropy([1,1])
entropy([1,1,1,1])
exit
from vector_utils import *
sparsity([0,1])
sparsity(np.array([0,1]))
sparsity(np.array([0,2]))
sparsity(np.array([0,1]))
sparsity(np.array([0,0]))
sparsity(np.array([0,0,0]))
sparsity(np.array([0,0,0,0]))
sparsity(np.array([0,0,0,1]))
sparsity(np.array([0,0,0,2]))
sparsity(np.array([0,0,0,1]))
sparsity(np.array([0,0,1,1]))
sparsity(np.array([0,1,1,1]))
sparsity(np.array([1,1,1,1]))
sparsity(np.array([0,0,0,0]))
sparsity(np.array([0,0,0]))
sparsity(np.array([0,0]))
math.sqrt(3) - math.sqrt(2)
math.sqrt(3) / math.sqrt(2)
math.sqrt(3) / (math.sqrt(3) - 1_
math.sqrt(3) / (math.sqrt(3) - 1)
sparsity(np.array([0]))
entropy([0,0,0,0])
entropy([0,0,0,1])
entropy([0,0,0,0])
import gdown
gdown.__version__
import torch
torch.__version__
import pytorch_lightning as pl
pl.load_from_checkpoint("checkpoints-beat-best/cpm")
pl.Module.load_from_checkpoint("checkpoints-beat-best/cpm")
from harmonic_inference.models.joint_model import MODEL_CLASSES
MODEL_CLASSES
MODEL_CLASSES["cpm"].load_from_checkpoint("checkpoints-beat-best/cpm/lightning_logs/version_0", weights_only=True)
import music21
from music21.harmony import *
Harmony
Harmony("Co")
Harmony("Co").write_as_string()
Harmony("Co").writeAsChord()
from glob import glob
glob("x") + glob("*")
glob("x")
[1,2,3]+[4,5,6]
from music21.harmony import ChordSymbol
ChordSymbol("C")
ChordSymbol("Cno3")
ChordSymbol("Cremove3")
ChordSymbol("C3")
ChordSymbol("C")
ChordSymbol("C").notes
ChordSymbol("Cno3").notes
from music21.harmony import ChordSymbol
ChordSymbol("Csus4/Aadd6")
import pytorch
import torch
torch.__version__
import pytorch_lightning
pytorch_lightning.__version__
import json
import requests
import gdown
import _pickle
import _pickle as cPickle
with open("download_urls", "rb") as input_file:
  download_urls = cPickle.load(input_file)
download_urls
download_urls[0]
download_urls[1]
download_urls[2]
download_urls[3]
download_urls[4]
download_urls[5]
import os
set(os.listdir(path))
set(os.listdir(download_urls[5]))
set(os.listdir(download_urls[5][1]))
from maas.song_generator import Song
import AMM_package
from maas.utils import get_melody_probability
from cylimiter import Limiter
import essentia
essentia.__version__
if []:
  print("Yes")
from maas_interface import *
from loopmasters_demo.maas_interface import *
MAAS_GENRE_DATA
get_config_data("genres.json")
final_genres = ["Random", "Random", "House"]
randomized_genres = []
for genre in final_genres:
                count_random = 0
                if "Random" in genre:
                    count_random += 1
                else:
                    randomized_genres.append(genre)
count_random
randomized_genres = []
            for genre in final_genres:
                count_random = 0
                if "Random" == genre:
                    count_random += 1
                else:
                    randomized_genres.append(genre)
randomized_genres = []
for genre in final_genres:
                count_random = 0
                if "Random" == genre:
                    count_random += 1
                else:
                    randomized_genres.append(genre)
count_random
final_genres
"Random" == final_genres[0]
"Random" == final_genres[1]
randomized_genres = []
count_random = 0
for genre in final_genres:
                if "Random" == genre:
                    count_random += 1
                else:
                    randomized_genres.append(genre)
count_random
randomized_genres
set(MAAS_GENRE_DATA) - set(randomized_genres)
random.sample(set(MAAS_GENRE_DATA) - set(randomized_genres), count_random)
import numpy as np
import json
json.loads('{
    "21": {
        "75624": "138.085",
        "77388": "148.685",
        "75054": "155.295",
        "77403": "157.532",
        "76173": "157.827",
        "77391": "158.092",
        "77205": "158.62",
        "75234": "159.227",
        "75027": "160.755",
        "75069": "161.198"
    },
    "29": {
        "75255": "138.934",
        "75042": "139.255",
        "77496": "139.335",
        "76779": "140.894",
        "75945": "141.24",
        "75036": "142.169",
        "76872": "142.763",
        "76806": "143.804",
        "75369": "145.239",
        "75480": "148.2"
    },
    "74832": {
        "74832": "0",
        "75813": "96.4348",
        "75786": "101.188",
        "75783": "101.502",
        "75465": "102.625",
        "75741": "103.22",
        "74937": "105.577",
        "75744": "106.883",
        "75801": "108.9",
        "75810": "112.246"
    }
}
l1 = [138, 148, 155, 157, 157, 158, 158, 159, 160, 161]
l2 = [138, 139, 139, 140, 141, 142, 142, 143, 145, 148]
l3 = [000, 096, 101, 101, 102, 103, 105, 106, 108, 112]
l3 = [0, 96, 101, 101, 102, 103, 105, 106, 108, 112]
l1
l2
l3
def sim(l):
    l = np.array(l)
    l = l - np.min(array)
    l = l / np.max(array)
    return 1 - l
sim(l1)
def sim(l):
    l = l - np.min(l)
    l = l / np.max(array)
    return 1 - l
sim(l1)
def sim(l):
    l = l - np.min(l)
    l = l / np.max(l)
    return 1 - l
sim(l1)
sim(l2)
sim(l3)
sim(l1)
def sim(l):
    l = l - np.min(l) + 0.1
    l = l / np.max(l)
    return 1 - l
sim(l2)
sim(l1)
sim(l3)
def sim(l):
    l = l - np.min(l)
    l = l / (np.max(l) + 0.1)
    return 1 - l
sim(l1)
sim(l2)
sim(l3)
import torch
torch.__version__
import librosa
librosa.__version__
import numpy
numpy.__version__
import pandas
pandas.__version__
import tqdm
tqdm.__version__
import transformers
transformers.__version__
import gradio
gradio.__version__
import essentia_tensorflow
import essentia-tensorflow
from src/download_parse_s3 import *
from src.download_parse_s3 import *
process_song_pari
process_song_pair
helper = MinioHelper(config.S3_ENDPOINT, config.S3_ACCESS_KEY, config.S3_SECRET_KEY)
bucket = config.S3_BUCKET
final_raw_dir = config.DRUMS_RAW_DIR
final_csv_dir = config.DRUMS_CSV_TRUTH_DIR
instrument == "Drums"
instrument = "Drums"
process_song_pair(helper, args.bucket, chart, final_raw_dir, final_csv_dir, args.instrument)
process_song_pair(helper, bucket, chart, final_raw_dir, final_csv_dir, instrument)
chart = "2. Arrival/notes.chart"
process_song_pair(helper, bucket, chart, final_raw_dir, final_csv_dir, instrument)
s3_chart_path = chart
song_name = re.sub(r'[<>:"/\\|?*]', '', os.path.basename(os.path.dirname(s3_chart_path))).strip()
song_name
local_song_dir = os.path.join(raw_dir, song_name)
local_song_dir = os.path.join(final_raw_dir, song_name)
os.makedirs(local_song_dir, exist_ok=True)
local_chart = os.path.join(local_song_dir, os.path.basename(s3_chart_path))
local_chart
helper.download_file(bucket, s3_chart_path, local_chart)
data = parse_chart_text(local_chart)
data
df = pd.DataFrame(data)
instrument = "drums"
process_song_pair(helper, bucket, chart, final_raw_dir, final_csv_dir, instrument)
import parangonar as pa
import partitura as pt
pt.load_score_midi(score_path)
gt = pt.load_score_midi(score_path)
transcription = pt.load_performance_midi(transcription_path)
gt = pt.load_score_midi("../../../windows/Downloads/Happy Birthday MIDI.mid")
trans = pt.load_performance_midi("../../../windows/Downloads/Happy Birthday MIDI.mid")
gt
gt.notes
gt.note_array()
gt_notes = gt.note_array(include_grace_notes=True)
trans_notes = trans.note_array()
matcher = pa.DualDTWNoteMatcher()
alignment = matcher(gt_notes, trans_notes, process_ornaments=True, score_part=gt)
matcher = pa.AutomaticNoteMatcher()
alignment = matcher(gt_notes, trans_notes, verbose_time=True)
alignment
matcher = pa.DualDTWNoteMatcher()
alignment = matcher(gt_notes, trans_notes, process_ornaments=True, score_part=gt)
alignment = matcher(gt_notes, trans_notes, process_ornaments=True)
alignment = matcher(gt_notes, trans_notes, process_ornaments=True, score_part=gt)
pt.io.exportmatch.save_match()
pt.io.exportmatch.save_match(alignment, trans, gt)
match_file = pt.io.exportmatch.save_match(alignment, trans, gt)
match_file
print(match_file)
str(match_file)
match_file.write("test.match")
import readline
with open("session.py", "w") as f:
    f.write("\n".join([readline.get_history_item(i) for i in range(1, readline.get_current_history_length() + 1)]))