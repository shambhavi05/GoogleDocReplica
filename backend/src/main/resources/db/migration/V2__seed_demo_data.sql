INSERT INTO users (id, name, email, password_hash, created_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Shambhavi', 'shambhavi@ajaia.com', NULL, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'Bob', 'bob@agij.com', NULL, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'Charlie', 'charlie@agij.com', NULL, CURRENT_TIMESTAMP);

INSERT INTO documents (id, title, content, owner_id, version, created_at, updated_at) VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Product Roadmap',
        '{"type":"doc","content":[{"type":"heading","attrs":{"level":1},"content":[{"type":"text","text":"Product Roadmap"}]},{"type":"paragraph","content":[{"type":"text","text":"Q1 2026 priorities for the DocSpace product line. This document tracks what we are building, why it matters, and who owns each workstream."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Goals"}]},{"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Ship collaborative editing"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Improve sharing permissions"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Reduce time-to-first-document for new users"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Ship Markdown and TXT import"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Timeline"}]},{"type":"orderedList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Week 1-2: Editor and autosave"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Week 3: Sharing and permissions"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Week 4: File import and polish"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Open questions"}]},{"type":"paragraph","content":[{"type":"text","text":"Do we need real-time multi-cursor editing for the first release, or is optimistic-lock autosave enough for the initial audience? Bob and Charlie, please weigh in with comments before Friday."}]}]}',
        '11111111-1111-1111-1111-111111111111',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab',
        'Design Notes',
        '{"type":"doc","content":[{"type":"heading","attrs":{"level":1},"content":[{"type":"text","text":"Design Notes"}]},{"type":"paragraph","content":[{"type":"text","text":"Early ideas for the DocSpace visual direction. Goal: a clean, modern SaaS productivity feel — not a generic admin dashboard."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Visual direction"}]},{"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Light background, subtle borders and shadows"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Green accent color for primary actions"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Generous whitespace, consistent spacing scale"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Typography"}]},{"type":"paragraph","content":[{"type":"text","text":"Using a system sans-serif stack for performance, with a slightly tighter letter-spacing on headings for a more editorial, confident feel."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Open design questions"}]},{"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Should the editor toolbar float or stay fixed at the top?"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Do we need a dark mode for v1?"}]}]}]}]}',
        '11111111-1111-1111-1111-111111111111',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaac',
        'Interview Notes',
        '{"type":"doc","content":[{"type":"heading","attrs":{"level":1},"content":[{"type":"text","text":"Interview Notes"}]},{"type":"paragraph","content":[{"type":"text","text":"Candidate feedback and follow-up questions from the panel round, consolidated for the hiring debrief."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Strengths"}]},{"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Strong system design fundamentals"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Clear communicator, walked through trade-offs well"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Asked good clarifying questions before diving into the solution"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Areas to probe further"}]},{"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Limited hands-on experience with distributed systems at scale"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Would like to see a coding sample under time pressure"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Recommendation"}]},{"type":"paragraph","content":[{"type":"text","text":"Lean positive. Suggest a follow-up pairing session focused on debugging an unfamiliar codebase."}]}]}',
        '11111111-1111-1111-1111-111111111111',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaad',
        'Weekly Journal',
        '{"type":"doc","content":[{"type":"heading","attrs":{"level":1},"content":[{"type":"text","text":"Weekly Journal"}]},{"type":"paragraph","content":[{"type":"text","text":"A running log of what shipped, what broke, and what I learned this week."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Shipped"}]},{"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"File import for TXT and Markdown"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Sharing modal with revoke support"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Broke"}]},{"type":"paragraph","content":[{"type":"text","text":"Autosave briefly fired on page load for viewers before the editor finished mounting — fixed by gating the update handler until the document and permissions are confirmed."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Learned"}]},{"type":"paragraph","content":[{"type":"text","text":"Optimistic locking with a version column is a simple, honest way to handle concurrent edits without building a full CRDT system."}]}]}',
        '11111111-1111-1111-1111-111111111111',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Meeting Notes - Kickoff',
        '{"type":"doc","content":[{"type":"heading","attrs":{"level":1},"content":[{"type":"text","text":"Meeting Notes - Kickoff"}]},{"type":"paragraph","content":[{"type":"text","text":"Notes from the project kickoff meeting with the full team."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Attendees"}]},{"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Shambhavi — Product"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Bob — Engineering"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Charlie — Design"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Decisions"}]},{"type":"orderedList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Ship a modular monolith instead of microservices for v1"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Use session-cookie auth for the demo, revisit for production"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Action items"}]},{"type":"paragraph","content":[{"type":"text","text":"Bob to draft the data model. Charlie to mock up the dashboard. Shambhavi to write the product brief."}]}]}',
        '22222222-2222-2222-2222-222222222222',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbc',
        'Engineering Onboarding Guide',
        '{"type":"doc","content":[{"type":"heading","attrs":{"level":1},"content":[{"type":"text","text":"Engineering Onboarding Guide"}]},{"type":"paragraph","content":[{"type":"text","text":"Everything a new engineer needs for their first week on the DocSpace team."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Checklist"}]},{"type":"orderedList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Set up local dev environment (Java 17, Node 20, Docker)"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Get access to the shared drive and the demo accounts"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Run the backend and frontend locally following the README"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Ship a first small PR by end of week one"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Useful links"}]},{"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"ARCHITECTURE.md for the data model and authorization strategy"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"AI_WORKFLOW.md for how this project was built"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Who to ask"}]},{"type":"paragraph","content":[{"type":"text","text":"Ping Shambhavi for product questions, Bob for backend, Charlie for anything design-related."}]}]}',
        '22222222-2222-2222-2222-222222222222',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        'Marketing Launch Plan',
        '{"type":"doc","content":[{"type":"heading","attrs":{"level":1},"content":[{"type":"text","text":"Marketing Launch Plan"}]},{"type":"paragraph","content":[{"type":"text","text":"Go-to-market plan for the DocSpace public launch."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Channels"}]},{"type":"bulletList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Product Hunt launch day post"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Newsletter announcement"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Twitter/X thread with a short demo clip"}]}]}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Messaging"}]},{"type":"paragraph","content":[{"type":"text","text":"Lead with \"a lightweight, collaborative document editor your whole team can actually use\" — emphasize simplicity over feature count."}]},{"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Timeline"}]},{"type":"orderedList","content":[{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"T-2 weeks: finalize landing page copy"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"T-1 week: brief the Product Hunt hunter"}]}]},{"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"Launch day: coordinated posts across all channels"}]}]}]}]}',
        '33333333-3333-3333-3333-333333333333',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

INSERT INTO document_shares (id, document_id, shared_with_id, permission, created_at) VALUES
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '22222222-2222-2222-2222-222222222222',
        'EDITOR',
        CURRENT_TIMESTAMP
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccd',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '33333333-3333-3333-3333-333333333333',
        'VIEWER',
        CURRENT_TIMESTAMP
    ),
    (
        'cccccccc-cccc-cccc-cccc-ccccccccccce',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbc',
        '11111111-1111-1111-1111-111111111111',
        'EDITOR',
        CURRENT_TIMESTAMP
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccf',
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        '11111111-1111-1111-1111-111111111111',
        'VIEWER',
        CURRENT_TIMESTAMP
    );
