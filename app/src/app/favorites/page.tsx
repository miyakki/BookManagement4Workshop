import Image from 'next/image';

const books = [
  {
    title: '嫌われる勇気',
    author: '岸見一郎、古賀史健',
    image: '/books/kirawareru.jpg',
    description: 'アドラー心理学をベースに、自分らしく生きる勇気を与えてくれる一冊。'
  },
  {
    title: 'FACTFULNESS（ファクトフルネス）',
    author: 'ハンス・ロスリング',
    image: '/books/factfulness.jpg',
    description: '世界を正しく見る力を育てる、知的でポジティブな教養本。'
  },
  {
    title: '小さな習慣',
    author: 'スティーヴン・ガイズ',
    image: '/books/smallhabits.jpg',
    description: '毎日続けられる小さな行動から人生を変える方法を解説。'
  }
];

export default function FavoriteBooks() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 text-white py-12 px-4">
      <h1 className="text-4xl font-bold text-center mb-12 tracking-wide">
        📚 私の好きな本 3選
      </h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-7xl mx-auto">
        {books.map((book, index) => (
          <div
            key={index}
            className="bg-gray-800 rounded-xl shadow-lg transform hover:scale-105 hover:shadow-2xl transition duration-300 ease-in-out"
          >
            <div className="relative h-64 rounded-t-xl overflow-hidden">
              <Image
                src={book.image}
                alt={book.title}
                layout="fill"
                objectFit="cover"
                className="hover:opacity-90 transition duration-200"
              />
            </div>
            <div className="p-6">
              <h2 className="text-2xl font-semibold mb-2">{book.title}</h2>
              <p className="text-sm text-gray-400 mb-4">著者：{book.author}</p>
              <p className="text-sm text-gray-300">{book.description}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}