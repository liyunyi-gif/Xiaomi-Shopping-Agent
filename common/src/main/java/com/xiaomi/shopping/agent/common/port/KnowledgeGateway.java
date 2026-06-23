package com.xiaomi.shopping.agent.common.port;

import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;

/**
 * Knowledge 子节点能力端口。
 *
 * @author liyunyi
 */
public interface KnowledgeGateway {

    KnowledgeResponse ask(KnowledgeRequest request);
}
