import tkinter as tk
from tkinter import messagebox
from PIL import Image, ImageTk
import random
import os
import datetime

from model import wczytaj_znaki_z_json, ZnakDrogowy
from wyniki import zapisz_wynik

ilosc_pytan = 4  # liczba przycisków z odpowiedziami do wyboru

class AplikacjaGUI:
    def __init__(self, root, sciezka_json="znaki.json", folder_obrazkow="obrazki"):
        self.root = root
        self.root.title("Nauka znaków drogowych")
        self.folder_obrazkow = folder_obrazkow

        # Ustawienie wymiarów okna i jego wyśrodkowanie na ekranie
        szerokosc = 600
        wysokosc = 500
        self.root.geometry(f"{szerokosc}x{wysokosc}")
        self.root.update_idletasks()
        ekran_szer = self.root.winfo_screenwidth()
        ekran_wys = self.root.winfo_screenheight()
        x = (ekran_szer // 2) - (szerokosc // 2)
        y = (ekran_wys // 2) - (wysokosc // 2)
        self.root.geometry(f"{szerokosc}x{wysokosc}+{x}+{y}")

        # Wczytanie znaków z pliku JSON
        self.znaki = wczytaj_znaki_z_json(sciezka_json)
        if not self.znaki:
            messagebox.showerror("Błąd", "Nie udało się wczytać znaków.")
            root.quit()  # zamknięcie aplikacji jeśli brak znaków
            return

        self.poprawne = 0               # licznik poprawnych odpowiedzi
        self.liczba_pytan = len(self.znaki)  # całkowita liczba pytań (znaków)
        random.shuffle(self.znaki)      # losowe przemieszanie listy znaków
        self.indeks = 0                 # aktualny indeks znaku (pytania)

        # Widget do wyświetlania obrazka znaku
        self.label_obraz = tk.Label(root)
        self.label_obraz.pack(pady=10)

        # Widget do wyświetlania pytania
        self.label_pytanie = tk.Label(root, text="", font=("Arial", 16))
        self.label_pytanie.pack(pady=10)

        # Tworzenie przycisków z odpowiedziami - 4 przyciski
        self.przyciski = []
        for i in range(ilosc_pytan):
            btn = tk.Button(root, text="", width=40, command=lambda i=i: self.sprawdz_odpowiedz(i))
            btn.pack(pady=5)
            self.przyciski.append(btn)

        # Label do statusu np. "Znak 1 z 10"
        self.label_status = tk.Label(root, text="", fg="gray")
        self.label_status.pack()

        # Wyświetlenie pierwszego znaku
        self.wyswietl_znak()

    def wyswietl_znak(self):
        znak: ZnakDrogowy = self.znaki[self.indeks]

        # Wczytanie i wyświetlenie obrazka znaku
        sciezka = os.path.join(self.folder_obrazkow, znak.plik_obrazka)
        try:
            obraz = Image.open(sciezka).resize((150, 150))
            self.img = ImageTk.PhotoImage(obraz)
            self.label_obraz.config(image=self.img)
            self.label_obraz.image = self.img
        except Exception as e:
            # Jeśli błąd przy ładowaniu obrazka, wyświetl tekst z błędem
            self.label_obraz.config(text=f"Błąd obrazu: {znak.plik_obrazka}")
            print(e)

        # Ustawienie pytania na labelu
        self.label_pytanie.config(text=znak.pytanie)

        # Przygotowanie listy odpowiedzi: poprawna + 3 losowe inne
        inne_odpowiedzi = [z.nazwa for z in self.znaki if z.nazwa != znak.poprawna]
        odpowiedzi = random.sample(inne_odpowiedzi, ilosc_pytan - 1)
        odpowiedzi.append(znak.poprawna)
        random.shuffle(odpowiedzi)
        self.aktualne_odpowiedzi = odpowiedzi

        # Przypisanie tekstów do przycisków
        for i in range(ilosc_pytan):
            self.przyciski[i].config(text=self.aktualne_odpowiedzi[i])

        # Aktualizacja statusu (np. "Znak 3 z 20")
        self.label_status.config(text=f"Znak {self.indeks + 1} z {self.liczba_pytan}")

    def sprawdz_odpowiedz(self, idx):
        # Sprawdzenie wybranej odpowiedzi po kliknięciu przycisku
        wybrana = self.aktualne_odpowiedzi[idx]
        znak = self.znaki[self.indeks]

        if znak.sprawdz_odpowiedz(wybrana):
            self.poprawne += 1
            messagebox.showinfo("Dobrze", "✅ To poprawna odpowiedź!")
        else:
            messagebox.showwarning("Źle", f"❌ Zła odpowiedź!\nPoprawna: {znak.poprawna}")

        # Po krótkim opóźnieniu przejdź do kolejnego znaku
        self.root.after(100, self.nastepny_znak)

    def nastepny_znak(self):
        if self.indeks == self.liczba_pytan - 1:
            # Jeśli to ostatni znak, zapisz wynik i zakończ quiz
            self.zapisz_wynik_do_pliku()
            messagebox.showinfo("Koniec", f"Quiz zakończony!\nTwój wynik: {self.poprawne}/{self.liczba_pytan}")
            self.root.quit()
        else:
            # Przejdź do kolejnego znaku i wyświetl go
            self.indeks += 1
            self.wyswietl_znak()

    def zapisz_wynik_do_pliku(self, nazwa_pliku="wyniki.txt"):
        try:
            zapisz_wynik(nazwa_pliku, self.poprawne, self.liczba_pytan)
        except IOError as e:
            # Wyświetl komunikat błędu, jeśli zapis nie powiódł się
            messagebox.showerror("Błąd zapisu", str(e))
