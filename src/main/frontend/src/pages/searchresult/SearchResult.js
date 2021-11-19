import {searchMovie} from "../../api/apicalls";
import React, {useEffect, useState} from "react";
import {Helmet} from "react-helmet";

export function SearchResult(props) {

    const [searchTerm, setSearchTerm] = useState(new URLSearchParams(window.location.search).get("term"));

    const [results, setResults] = useState([]);

    useEffect(() => {
        searchMovie({searchTerm: searchTerm, page: new URLSearchParams(window.location.search).get("page")}).then((response) => {
            setResults(response.data.map((movie) => (
                <li>
                    <div id={movie.id} onClick={handleMovieClick}>
                        {movie.title}
                    </div>
                    <div>
                        <img alt="pic" src={"https://image.tmdb.org/t/p/original" + movie.poster_path} width="100px"/>
                    </div>
                </li>
            )));

        });
    }, [new URLSearchParams(window.location.search).get("page")])

    function handleMovieClick(event) {
        props.history.push("/movie?" + event.target.id);
    }

    function previousPage() {
        if (parseInt(new URLSearchParams(window.location.search).get("page")) !== 0)
            props.history.push("/search?term=" + searchTerm + "&page=" + (parseInt(new URLSearchParams(window.location.search).get("page")) - 1).toString());
        else
            props.history.push("/search?term=" + searchTerm + "&page=" + (parseInt(new URLSearchParams(window.location.search).get("page"))).toString());
    }

    function nextPage() {
        //setPage((parseInt(page) + 1).toString())
        //(parseInt(new URLSearchParams(window.location.search).get("page"))+1).toString()
        props.history.push("/search?term=" + searchTerm + "&page=" + (parseInt(new URLSearchParams(window.location.search).get("page")) + 1).toString());
    }

    return (
        <div id="searchresult">
            <Helmet>
                <meta charSet="UTF-8"/>
                <title>{searchTerm}</title>
            </Helmet>
            <ul id="searchlist">
                {results}
            </ul>
            <button onClick={previousPage}>Previous</button>
            <button onClick={nextPage}>Next</button>
        </div>);
}