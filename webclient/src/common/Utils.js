export function getSupportedLanguages(books) {
    const languageSet = new Set();
    for (const book of books) {
        for (const translation of book.translations) {
            languageSet.add(translation.language);
        }
    }
    return Array.from(languageSet).sort();
}