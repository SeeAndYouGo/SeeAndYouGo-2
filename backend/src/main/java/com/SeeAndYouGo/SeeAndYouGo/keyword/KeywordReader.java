package com.SeeAndYouGo.SeeAndYouGo.keyword;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.EntityNotFoundException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KeywordReader {
    private final KeywordRepository keywordRepository;

    public Keyword getByName(String name) {
        return keywordRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.KEYWORD_NOT_FOUND, "name=" + name));
    }

    public Optional<Keyword> findByName(String name) {
        return keywordRepository.findByName(name);
    }

    public Keyword getOrCreate(String name) {
        return keywordRepository.findByName(name)
                .orElseGet(() -> keywordRepository.save(new Keyword(name)));
    }
}
