export function BookItem({title, id, language}) {
  return (
    <a className="book-item" href={`/books/${id}?lang=${language}`}>
      <span className="book-item__icon" aria-hidden="true">
        <i className="fa-solid fa-book-open" />
      </span>
      <span className="book-item__title">{title}</span>
      <span className="book-item__arrow" aria-hidden="true">
        <i className="fa-solid fa-arrow-right" />
      </span>
    </a>
  );
}
