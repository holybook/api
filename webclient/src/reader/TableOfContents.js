import React, {useState} from 'react';
import {scrollToIndex} from './ScrollPosition';

/**
 * A collapsible side menu listing the sections of a book, derived from the SECTION_TITLE paragraphs
 * already present in the loaded stream. Renders nothing for flat books (no sections).
 */
export function TableOfContents({paragraphs}) {
  const [open, setOpen] = useState(true);

  const entries = paragraphs.filter((p) => p.type === 'section-title');
  if (entries.length === 0) {
    return null;
  }

  return (
    <aside className={`toc ${open ? 'toc--open' : 'toc--closed'}`}>
      <button className="toc__toggle" onClick={() => setOpen(!open)}
              aria-expanded={open} title="Table of contents">
        <i className={`fa-solid ${open ? 'fa-list-ul' : 'fa-bars'}`}
           aria-hidden="true"/>
        {open && <span className="toc__toggle-label">Contents</span>}
      </button>
      {open &&
        <nav className="toc__list" aria-label="Table of contents">
          {entries.map((entry) => {
            const depth = entry.sectionPath
              ? entry.sectionPath.split('.').length : 1;
            return (
              <button
                key={entry.index}
                className={`toc__entry toc__entry--l${Math.min(depth, 4)}`}
                onClick={() => scrollToIndex(entry.index)}
                title={entry.text}>
                {entry.sectionPath &&
                  <span className="toc__number">{entry.sectionPath}</span>}
                <span className="toc__text">{entry.text}</span>
              </button>
            );
          })}
        </nav>}
    </aside>
  );
}
