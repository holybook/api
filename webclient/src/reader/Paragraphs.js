import React, {useLayoutEffect, useRef, useState} from 'react';
import {setTopPosition} from "./ScrollPosition";

export function Paragraphs({book, language, scrollPosition}) {

    const [state, setState] = useState({
        language: null,
        paragraphs: []
    });

    function fetchParagraphs(language) {
        fetch(
            `/api/books/${book.id}/paragraphs?lang=${language}`)
            .then(response => {
                return response.json()
            })
            .then(paragraphs => {
                setState({
                    language: language,
                    paragraphs: paragraphs
                })
            })
    }

    if (state.language !== language) {
        // refetch
        fetchParagraphs(language)
    }

    if (state.language === null) {
        return <div>
            Loading...
        </div>
    }

    return state.paragraphs.map((paragraph) =>
        <Paragraph
            paragraph={paragraph}
            key={paragraph.index}
            scrollPosition={scrollPosition}/>
    )
}

function Paragraph({paragraph, scrollPosition}) {

    const ref = useRef(null);
    useLayoutEffect(() => {
        const topPosition = ref.current.offsetTop;
        setTopPosition(paragraph.index, topPosition);
        if (scrollPosition !== null && paragraph.index
            === scrollPosition.index) {
            document.getElementById('content').scrollTo({
                top: topPosition - scrollPosition.offset
            });
        }
    });

    return (<p ref={ref} className={`par ${paragraph.type}`}>
        { paragraph.number && <span className="number">{paragraph.number}</span> } {paragraph.text}
    </p>);
}