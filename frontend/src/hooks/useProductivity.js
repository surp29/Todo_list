import { useEffect, useState } from 'react';
import * as productivityApi from '../api/productivityApi';

export default function useProductivity() {
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    productivityApi
      .getProductivityOverview()
      .then(setOverview)
      .catch(() => setError('Không thể tải dữ liệu năng suất'))
      .finally(() => setLoading(false));
  }, []);

  return { overview, loading, error };
}
