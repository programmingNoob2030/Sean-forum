import type {
  PostContentDocument,
  PostContentFormat,
  PostContentNode,
  PostContentTextNode,
  PostContentView,
} from '@/models/post/postTypes'

const SUPPORTED_VERSION = 1

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function normalizeText(value: unknown) {
  return typeof value === 'string' ? value.replace(/\u200B/g, '') : ''
}

function normalizeNode(node: unknown): PostContentNode | null {
  if (!isRecord(node) || typeof node.type !== 'string') {
    return null
  }

  if (node.type === 'text') {
    return {
      type: 'text',
      text: normalizeText(node.text),
    } satisfies PostContentTextNode
  }

  if (node.type === 'image' && typeof node.path === 'string' && node.path.trim()) {
    return {
      type: 'image',
      path: node.path.trim(),
    }
  }

  return null
}

function normalizeNodes(nodes: unknown): PostContentNode[] {
  if (!Array.isArray(nodes)) {
    return []
  }

  return nodes.map(normalizeNode).filter((node): node is PostContentNode => node !== null)
}

function parseContentDocument(content: string): PostContentDocument | null {
  if (!content.trim()) {
    return null
  }

  try {
    const parsed: unknown = JSON.parse(content)
    if (!isRecord(parsed) || parsed.version !== SUPPORTED_VERSION) {
      return null
    }

    return {
      version: SUPPORTED_VERSION,
      nodes: normalizeNodes(parsed.nodes),
    }
  } catch {
    return null
  }
}

function buildTextPreview(nodes: PostContentNode[]) {
  let preview = ''

  for (const node of nodes) {
    if (node.type === 'text') {
      preview += node.text
      continue
    }

    if (node.type === 'image' && preview.length > 0 && !/\s$/.test(preview)) {
      preview += '\n'
    }
  }

  return preview
    .replace(/\u200B/g, '')
    .replace(/[ \t]+\n/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function firstImagePath(nodes: PostContentNode[]) {
  const imageNode = nodes.find((node) => node.type === 'image')
  return imageNode?.type === 'image' ? imageNode.path : undefined
}

export function resolvePostContentView(
  content?: string | null,
  contentFormat?: PostContentFormat,
  contentNodes?: PostContentNode[] | null,
  contentTextPreview?: string | null,
  firstImage?: string | null,
): PostContentView {
  const normalizedNodes = normalizeNodes(contentNodes)
  const normalizedPreview = contentTextPreview?.trim() ?? ''
  const normalizedFirstImage = firstImage?.trim() ?? ''

  if (normalizedNodes.length > 0) {
    return {
      format: contentFormat ?? 'BLOCKS',
      nodes: normalizedNodes,
      textPreview: normalizedPreview || buildTextPreview(normalizedNodes),
      firstImagePath: normalizedFirstImage || firstImagePath(normalizedNodes),
    }
  }

  if (contentFormat === 'PLAIN') {
    const plainText = normalizeText(content ?? '')
    return {
      format: 'PLAIN',
      nodes: plainText ? [{ type: 'text', text: plainText } satisfies PostContentTextNode] : [],
      textPreview: normalizedPreview || plainText,
      firstImagePath: normalizedFirstImage || undefined,
    }
  }

  const parsedDocument = parseContentDocument(content ?? '')
  if (parsedDocument && parsedDocument.nodes.length > 0) {
    return {
      format: 'BLOCKS',
      nodes: parsedDocument.nodes,
      textPreview: normalizedPreview || buildTextPreview(parsedDocument.nodes),
      firstImagePath: normalizedFirstImage || firstImagePath(parsedDocument.nodes),
    }
  }

  const plainText = normalizeText(content ?? '')
  return {
    format: 'PLAIN',
    nodes: plainText ? [{ type: 'text', text: plainText } satisfies PostContentTextNode] : [],
    textPreview: normalizedPreview || plainText,
    firstImagePath: normalizedFirstImage || undefined,
  }
}
