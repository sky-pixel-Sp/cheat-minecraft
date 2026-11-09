import unittest

from skyop_password_generator import generate_password


class PasswordGeneratorTests(unittest.TestCase):
    def test_length_and_selected_groups_are_respected(self) -> None:
        password = generate_password(40, True, True, True, True, False)
        self.assertEqual(len(password), 40)
        self.assertTrue(any(char.isupper() for char in password))
        self.assertTrue(any(char.islower() for char in password))
        self.assertTrue(any(char.isdigit() for char in password))
        self.assertTrue(any(not char.isalnum() for char in password))

    def test_ambiguous_characters_are_excluded(self) -> None:
        for _ in range(100):
            password = generate_password(32, True, True, True, True, True)
            self.assertFalse(any(char in "O0Il1|'\"[]" for char in password))

    def test_one_group_can_be_used(self) -> None:
        password = generate_password(16, False, False, True, False, False)
        self.assertEqual(len(password), 16)
        self.assertTrue(password.isdigit())

    def test_empty_groups_are_rejected(self) -> None:
        with self.assertRaises(ValueError):
            generate_password(16, False, False, False, False, False)


if __name__ == "__main__":
    unittest.main()
