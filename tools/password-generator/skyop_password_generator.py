from __future__ import annotations

import secrets
import string
import tkinter as tk
from tkinter import filedialog, messagebox
from pathlib import Path


APP_BG = "#F2F5F9"
PANEL_BG = "#FFFFFF"
TEXT = "#17212F"
MUTED = "#65758B"
ACCENT = "#2864DC"
BORDER = "#D7DFEA"


def generate_password(
    length: int,
    use_upper: bool,
    use_lower: bool,
    use_digits: bool,
    use_symbols: bool,
    exclude_ambiguous: bool,
) -> str:
    groups: list[str] = []
    if use_upper:
        groups.append(string.ascii_uppercase)
    if use_lower:
        groups.append(string.ascii_lowercase)
    if use_digits:
        groups.append(string.digits)
    if use_symbols:
        groups.append("!@#$%^&*()-_=+[]{}:,.?")
    if not groups:
        raise ValueError("Select at least one character group.")
    if length < len(groups) or length > 128:
        raise ValueError("Password length must be between the selected groups and 128.")

    if exclude_ambiguous:
        ambiguous = set("O0Il1|'\\\"[]")
        groups = ["".join(char for char in group if char not in ambiguous) for group in groups]
        groups = [group for group in groups if group]
        if not groups:
            raise ValueError("The selected groups have no characters after filtering.")

    chosen = [secrets.choice(group) for group in groups]
    alphabet = "".join(groups)
    chosen.extend(secrets.choice(alphabet) for _ in range(length - len(chosen)))
    secrets.SystemRandom().shuffle(chosen)
    return "".join(chosen)


class PasswordGeneratorApp:
    def __init__(self, root: tk.Tk) -> None:
        self.root = root
        self.root.title("SkyOP Password Generator")
        self.root.geometry("560x610")
        self.root.minsize(500, 560)
        self.root.configure(bg=APP_BG)

        self.length = tk.IntVar(value=24)
        self.use_upper = tk.BooleanVar(value=True)
        self.use_lower = tk.BooleanVar(value=True)
        self.use_digits = tk.BooleanVar(value=True)
        self.use_symbols = tk.BooleanVar(value=True)
        self.exclude_ambiguous = tk.BooleanVar(value=True)
        self.password = tk.StringVar(value="")
        self.revealed = tk.BooleanVar(value=True)

        self._build_ui()
        self.generate()

    def _build_ui(self) -> None:
        outer = tk.Frame(self.root, bg=APP_BG, padx=24, pady=22)
        outer.pack(fill="both", expand=True)

        tk.Label(
            outer, text="SkyOP Security Utility", bg=APP_BG, fg=TEXT,
            font=("Segoe UI", 21, "bold")
        ).pack(anchor="w")
        tk.Label(
            outer, text="Create a strong random password on this device.",
            bg=APP_BG, fg=MUTED, font=("Segoe UI", 10)
        ).pack(anchor="w", pady=(4, 18))

        card = tk.Frame(outer, bg=PANEL_BG, highlightbackground=BORDER, highlightthickness=1, padx=18, pady=18)
        card.pack(fill="x")

        tk.Label(card, text="GENERATED PASSWORD", bg=PANEL_BG, fg=MUTED,
                 font=("Segoe UI", 9, "bold")).pack(anchor="w")
        password_row = tk.Frame(card, bg=PANEL_BG)
        password_row.pack(fill="x", pady=(8, 12))
        self.password_entry = tk.Entry(
            password_row, textvariable=self.password, font=("Consolas", 14),
            relief="flat", bg="#F6F8FC", fg=TEXT, readonlybackground="#F6F8FC",
            state="readonly", show=""
        )
        self.password_entry.pack(side="left", fill="x", expand=True, ipady=10)
        tk.Checkbutton(
            password_row, text="Reveal", variable=self.revealed,
            command=self._toggle_reveal, bg=PANEL_BG, fg=MUTED,
            activebackground=PANEL_BG, selectcolor=PANEL_BG,
            font=("Segoe UI", 9)
        ).pack(side="right", padx=(10, 0))

        buttons = tk.Frame(card, bg=PANEL_BG)
        buttons.pack(fill="x")
        tk.Button(
            buttons, text="Generate", command=self.generate, bg=ACCENT, fg="#FFFFFF",
            activebackground="#1E53C4", activeforeground="#FFFFFF",
            relief="flat", font=("Segoe UI", 10, "bold"), padx=18, pady=9, cursor="hand2"
        ).pack(side="left")
        tk.Button(
            buttons, text="Copy", command=self.copy, bg="#E8EEF8", fg=TEXT,
            activebackground="#DCE6F5", relief="flat",
            font=("Segoe UI", 10, "bold"), padx=18, pady=9, cursor="hand2"
        ).pack(side="left", padx=(8, 0))
        tk.Button(
            buttons, text="Save .txt", command=self.save, bg="#E8EEF8", fg=TEXT,
            activebackground="#DCE6F5", relief="flat",
            font=("Segoe UI", 10, "bold"), padx=18, pady=9, cursor="hand2"
        ).pack(side="left", padx=(8, 0))

        options = tk.Frame(outer, bg=PANEL_BG, highlightbackground=BORDER, highlightthickness=1, padx=18, pady=16)
        options.pack(fill="x", pady=(14, 0))
        tk.Label(options, text="PASSWORD SETTINGS", bg=PANEL_BG, fg=TEXT,
                 font=("Segoe UI", 10, "bold")).pack(anchor="w")
        length_row = tk.Frame(options, bg=PANEL_BG)
        length_row.pack(fill="x", pady=(12, 5))
        tk.Label(length_row, text="Length", bg=PANEL_BG, fg=MUTED,
                 font=("Segoe UI", 10)).pack(side="left")
        self.length_label = tk.Label(length_row, text="24 characters", bg=PANEL_BG,
                                     fg=TEXT, font=("Segoe UI", 10, "bold"))
        self.length_label.pack(side="right")
        tk.Scale(
            options, from_=8, to=64, orient="horizontal", variable=self.length,
            command=self._update_length_label, showvalue=False, resolution=1,
            bg=PANEL_BG, fg=TEXT, troughcolor="#DDE5F1", highlightthickness=0,
            activebackground=ACCENT, relief="flat"
        ).pack(fill="x")

        checks = tk.Frame(options, bg=PANEL_BG)
        checks.pack(fill="x", pady=(8, 0))
        for label, variable in (
            ("Uppercase (A–Z)", self.use_upper),
            ("Lowercase (a–z)", self.use_lower),
            ("Numbers (0–9)", self.use_digits),
            ("Symbols (!@#…)", self.use_symbols),
            ("Exclude ambiguous characters", self.exclude_ambiguous),
        ):
            tk.Checkbutton(
                checks, text=label, variable=variable, bg=PANEL_BG, fg=TEXT,
                activebackground=PANEL_BG, selectcolor="#E8EEF8",
                font=("Segoe UI", 9)
            ).pack(anchor="w", pady=2)

        note = (
            "Privacy: generation uses Python's cryptographic secrets module. "
            "Passwords are not uploaded or transmitted. Do not save a password "
            "on a shared computer."
        )
        tk.Label(outer, text=note, bg=APP_BG, fg=MUTED, justify="left",
                 wraplength=500, font=("Segoe UI", 9)).pack(anchor="w", pady=(15, 0))

    def _update_length_label(self, _value: str | None = None) -> None:
        self.length_label.configure(text=f"{self.length.get()} characters")

    def _toggle_reveal(self) -> None:
        self.password_entry.configure(show="" if self.revealed.get() else "•")

    def generate(self) -> None:
        try:
            value = generate_password(
                self.length.get(), self.use_upper.get(), self.use_lower.get(),
                self.use_digits.get(), self.use_symbols.get(), self.exclude_ambiguous.get()
            )
        except ValueError as exc:
            messagebox.showwarning("Password settings", str(exc), parent=self.root)
            return
        self.password.set(value)
        self._update_length_label()

    def copy(self) -> None:
        value = self.password.get()
        if not value:
            messagebox.showinfo("Nothing to copy", "Generate a password first.", parent=self.root)
            return
        self.root.clipboard_clear()
        self.root.clipboard_append(value)
        self.root.update()
        messagebox.showinfo("Copied", "Password copied to the clipboard.", parent=self.root)

    def save(self) -> None:
        value = self.password.get()
        if not value:
            messagebox.showinfo("Nothing to save", "Generate a password first.", parent=self.root)
            return
        destination = filedialog.asksaveasfilename(
            parent=self.root, title="Save password", defaultextension=".txt",
            initialfile="skyop-password.txt", filetypes=[("Text file", "*.txt")]
        )
        if not destination:
            return
        try:
            Path(destination).write_text(value + "\n", encoding="utf-8")
        except OSError as exc:
            messagebox.showerror("Save failed", str(exc), parent=self.root)
            return
        messagebox.showinfo("Saved", "Password saved. Keep this file private.", parent=self.root)


def main() -> None:
    root = tk.Tk()
    PasswordGeneratorApp(root)
    root.mainloop()


if __name__ == "__main__":
    main()
