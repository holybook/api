import React, {useLayoutEffect, useRef} from 'react';
import {setTopPosition} from "./ScrollPosition";

export function Paragraphs({paragraphs, scrollPosition, language}) {
  return paragraphs.map((paragraph) =>
    <Paragraph
      paragraph={paragraph}
      key={paragraph.index}
      scrollPosition={scrollPosition}
      language={language}/>
  )
}

/** Depth of a section path: "" -> 0, "1" -> 1, "1.1" -> 2, ... */
function sectionDepth(sectionPath) {
  return sectionPath ? sectionPath.split('.').length : 0;
}

/** The label shown next to a body paragraph, e.g. "1.1:5" or, for flat books, "5". */
function numberLabel(paragraph) {
  if (paragraph.number == null) {
    return null;
  }
  return paragraph.sectionPath
    ? `${paragraph.sectionPath}:${paragraph.number}`
    : `${paragraph.number}`;
}

function Paragraph({paragraph, scrollPosition, language}) {

  const ref = useRef(null);
  useLayoutEffect(() => {
    const topPosition = ref.current.offsetTop;
    setTopPosition(paragraph.index, topPosition);
    if (scrollPosition !== null && paragraph.index
      === scrollPosition.index) {
      document.getElementById('content-container').scrollTo({
        top: topPosition - scrollPosition.offset
      });
    }
  });

  if (paragraph.type === 'section-title') {
    const level = Math.min(Math.max(sectionDepth(paragraph.sectionPath), 1), 4);
    return (
      <div ref={ref} lang={language}
           className={`section-title section-title--l${level}`}>
        {paragraph.sectionPath &&
          <span className="section-title__number">{paragraph.sectionPath}</span>}
        <span className="section-title__text">{paragraph.text}</span>
      </div>
    );
  }

  const label = numberLabel(paragraph);
  return (<p ref={ref} lang={language} className={`par ${paragraph.type}`}>
    {label && <span className="par-number">{label}</span>} {paragraph.text}
  </p>);
}
