package com.resume.service;

import com.resume.entity.ShareLink;
import com.resume.repository.ShareLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShareLinkService {

    private final ShareLinkRepository repository;

    public ShareLinkService(ShareLinkRepository repository) {
        this.repository = repository;
    }

    public List<ShareLink> getLinks(String resumeId) {
        return repository.findByResumeIdOrderByCreatedAtDesc(resumeId);
    }

    @Transactional
    public ShareLink createLink(String resumeId, boolean desensitize) {
        ShareLink link = new ShareLink();
        link.setResumeId(resumeId);
        link.setEnabled(true);
        link.setDesensitize(desensitize);
        return repository.save(link);
    }

    @Transactional
    public void toggleLink(String id, boolean enabled) {
        ShareLink link = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Share link not found"));
        link.setEnabled(enabled);
        repository.save(link);
    }

    @Transactional
    public void deleteLink(String id) {
        repository.deleteById(id);
    }

    public ShareLink getPublicLink(String id) {
        ShareLink link = repository.findByIdAndEnabledTrue(id)
                .orElseThrow(() -> new RuntimeException("Share link not found or disabled"));
        if (link.getExpiresAt() != null && java.time.LocalDateTime.now().isAfter(link.getExpiresAt())) {
            throw new RuntimeException("Share link has expired");
        }
        return link;
    }
}
