import os
import json

LOCALE_MAP = {
    "cs-CZ": "cs_cz",
    "cy-GB": "cy_gb",
    "da-DK": "da_dk",
    "de-DE": "de_de",
    "en":    "_",
    "en-CA": "en_ca",
    "en-GB": "en_gb",
    "es-ES": "es_es",
    "es-MX": "es_mx",
    "fi-FI": "fi_fi",
    "fr-FR": "fr_fr",
    "hu-HU": "hu_hu",
    "it-IT": "it_it",
    "ja-JP": "ja_jp",
    "ko-KR": "ko_kr",
    "la":    "la_la",
    "nl-NL": "nl_nl",
    "pl-PL": "pl_pl",
    "pt-BR": "pt_br",
    "pt-PT": "pt_pt",
    "ru-RU": "ru_ru",
    "sk-SK": "sk_sk",
    "sv-SE": "sv_se",
    "th-TH": "th_th",
    "tr-TR": "tr_tr",
    "uk-UA": "uk_ua",
    "vec":   "_",
    "zh-Hans-CN": "zh_cn",

    # Special/Custom Codes
    "tkl": "tok",    # Toki Pona
    "xxc": "lol_us", # LOLCAT
    "xxd": "en_pt",  # Pirate Speak
    "xxe": "pm_ap"   # Full Aperture Terms
}

def process_translations():
    input_dir = "localazy_download"
    output_dir = "output"
    os.makedirs(output_dir, exist_ok=True)

    for old_code, mc_code in LOCALE_MAP.items():
        if mc_code == "_":
            continue

        source_path = os.path.join(input_dir, old_code, "lang.json")

        if os.path.exists(source_path):
            with open(source_path, "r", encoding="utf-8") as f:
                data = json.load(f)

            output_path = os.path.join(output_dir, f"{mc_code}.json")
            with open(output_path, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, sort_keys=True, ensure_ascii=False)

if __name__ == "__main__":
    process_translations()